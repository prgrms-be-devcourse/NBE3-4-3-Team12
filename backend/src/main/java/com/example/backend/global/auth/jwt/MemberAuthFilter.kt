package com.example.backend.global.auth.jwt

import com.example.backend.domain.member.exception.MemberException
import com.example.backend.global.auth.exception.AuthErrorCode
import com.example.backend.global.auth.exception.AuthException
import com.example.backend.global.auth.kakao.service.KakaoAuthService
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.auth.service.CustomUserDetailService
import com.example.backend.global.auth.util.JwtUtil
import com.example.backend.global.auth.util.TokenProvider
import com.example.backend.global.redis.service.RedisService
import com.example.backend.global.response.ErrorResponse.Companion.of
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

/**
 * MemberAuthFilter
 * 사용자 자체 Jwt토큰 필터
 * @author 100minha
 */
@Configuration
class MemberAuthFilter(
    private val cookieService: CookieService,
    private val customUserDetailService: CustomUserDetailService,
    private val kakaoAuthService: KakaoAuthService,
    private val redisService: RedisService,
    private val jwtUtil: JwtUtil,
    private val tokenProvider: TokenProvider,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(MemberAuthFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = cookieService.getAccessTokenFromCookie(request)
        val refreshToken = cookieService.getRefreshTokenFromCookie(request)

        // 엑세스 토큰과 리프레쉬 토큰이 null일 경우 에러 응답
        // 엑세스 토큰만 null일 경우 리프레쉬 토큰으로 토큰 재발급 후 인증
        if (accessToken == null) {
            if (refreshToken == null) {
                handleAuthError(AuthException(AuthErrorCode.AUTHORIZATION_FAILED), "", request, response)
                return
            }
            try {
                val reissuedAccessToken = reissueToken(refreshToken, request, response)
                setAuthenticationInContext(reissuedAccessToken)
                filterChain.doFilter(request, response)
            } catch (e: Exception) {
                handleAuthError(AuthException(AuthErrorCode.TOKEN_REISSUE_FAILED), refreshToken, request, response)
            }
            return
        }

        try {
            val tokenStatus = jwtUtil.validateToken(accessToken)

            when (tokenStatus) {
                TokenStatus.VALID -> setAuthenticationInContext(accessToken)
                TokenStatus.EXPIRED -> {
                    log.info("만료된 토큰입니다.")
                    val reissuedAccessToken = reissueToken(
                        refreshToken ?: throw AuthException(AuthErrorCode.INVALID_TOKEN),
                        request, response
                    )
                    setAuthenticationInContext(reissuedAccessToken)
                }

                TokenStatus.MALFORMED, TokenStatus.INVALID -> {
                    log.error("잘못된 형식의 토큰입니다.")
                    handleAuthError(AuthException(AuthErrorCode.INVALID_TOKEN), refreshToken ?: "", request, response)
                    return
                }
            }
        } catch (e: Exception) {
            log.error("필터 내부에서 예상치 못한 예외 발생: {}", e.message)
            handleAuthError(AuthException(AuthErrorCode.AUTHORIZATION_FAILED), refreshToken ?: "", request, response)
            return
        }
        filterChain.doFilter(request, response)
    }

    /**
     * 사용자 권한이 필요한 api 경로만 필터링을 하는 메서드
     * @param request
     * @return
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val method = request.method

        return !(
                (path == "/groups/member" && method == "GET") ||
                        (path == "/groups/location" && method == "GET") ||
                        (path.startsWith("/groups") && method in setOf("POST", "PUT", "DELETE")) ||
                        path.startsWith("/members") ||
                        path.startsWith("/votes") ||
                        path.startsWith("/voters") ||
                        path == "/auth/kakao/logout"
                )
    }

    /**
     * SecurityContext에 인증 정보를 주입하는 메서드
     * @param accessToken
     */
    private fun setAuthenticationInContext(accessToken: String) {
        val customUserDetails = customUserDetailService.loadUserByAccessToken(accessToken)
        val authenticationToken = UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authenticationToken
    }

    /**
     * 인증에 실패했을 때 로그아웃 처리 및 에러 응답 메서드
     * @param ex
     * @param request
     * @param response
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun handleAuthError(
        ex: AuthException, refreshToken: String,
        request: HttpServletRequest, response: HttpServletResponse
    ) {
        redisService.addBlackList(refreshToken, jwtUtil.getRefreshTokenExpirationTime())
        cookieService.clearTokenFromCookie(response)
        SecurityContextHolder.clearContext()

        response.status = ex.status.value()
        response.contentType = "application/json;charset=UTF-8"

        val errorResponse = of(ex.message, ex.code, request.requestURI)
        objectMapper.writeValue(response.writer, errorResponse)
    }

    /**
     * 토큰 재발급 및 쿠키에 토큰 정보 저장하는 메서드
     * @param refreshToken
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun reissueToken(
        refreshToken: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        try {
            val memberTokenReissueDto = kakaoAuthService.reissueTokens(refreshToken)

            val reissuedAccessToken = tokenProvider.generateMemberAccessToken(
                memberTokenReissueDto.id, memberTokenReissueDto.nickname,
                memberTokenReissueDto.email
            )

            cookieService.addAccessTokenToCookie(reissuedAccessToken, response)
            cookieService.addRefreshTokenToCookieWithSameSiteNone(memberTokenReissueDto.refreshToken, response)

            return reissuedAccessToken
        } catch (e: MemberException) {
            log.error("유효하지 않은 인증정보입니다.")
            handleAuthError(AuthException(AuthErrorCode.INVALID_TOKEN), refreshToken, request, response)
            throw e
        } catch (e: Exception) {
            log.error("토큰 갱신 중 오류 발생: {}", e.message)
            handleAuthError(AuthException(AuthErrorCode.TOKEN_REISSUE_FAILED), refreshToken, request, response)
            throw e
        }
    }
}
