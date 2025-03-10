package com.example.backend.global.auth.kakao.controller

import com.example.backend.global.auth.kakao.dto.KakaoTokenResponseDto
import com.example.backend.global.auth.kakao.dto.KakaoUserInfoResponseDto
import com.example.backend.global.auth.kakao.service.KakaoAuthService
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.response.ApiResponse
import com.example.backend.global.response.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * KakaoAuthController
 * 사용자 인증 관련 controller입니다
 * @author 100minha
 */
@RequestMapping("/auth/kakao")
@RestController
class KakaoAuthController(
    private val kakaoAuthService: KakaoAuthService,
    private val cookieService: CookieService,
    @Value("\${CLIENT_BASE_URL}") private val clientBaseUrl: String
) {
    /**
     * 카카오 로그인 페이지로 리다이렉트 및
     * 인가 토큰 발급 요청하는 메서드
     * @return
     */
    @GetMapping("/login")
    fun kakaoLogin(): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.location = URI.create(kakaoAuthService.getKakaoAuthorizationUrl())

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).body(null)
    }

    /**
     * 카카오에서 발급 받은 인가 토큰으로 access토큰, refresh토큰 요청하는 메서드
     * @param authorizationCode
     * @return
     */
    @GetMapping("/callback")
    fun authorizeAndLoginWithKakao(
        @RequestParam(value = "code", required = false) authorizationCode: String,
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "error-description", required = false) errorDescription: String?,
        request: HttpServletRequest, response: HttpServletResponse
    ): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.location = URI.create(clientBaseUrl)

        // 카카오에서 인가 토큰이 아닌 에러를 반환할 시 홈페이지로 리다이렉트 및 에러 메세지 응답
        if (error != null) {
            val errorResponse = ErrorResponse(
                message = errorDescription ?: "UNKNOWN_ERROR",
                code = "400-$error",
                requestUri = request.requestURI
            )

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                .body(errorResponse)
        }

        val kakaoTokenDto = kakaoAuthService.getTokenFromKakao(authorizationCode)
        val kakaoUserInfoDto = kakaoAuthService.getUserInfo(kakaoTokenDto.accessToken)

        val kakaoId = kakaoUserInfoDto.id

        // 기존 사용인지 검증 후 신규 사용자일 시 회원가입 진행
        checkNewMemberAndJoin(kakaoId, kakaoUserInfoDto)

        // 로그인 진행
        return login(kakaoTokenDto, kakaoId, response, headers)
    }

    private fun checkNewMemberAndJoin(kakaoId: Long, kakaoUserInfoDto: KakaoUserInfoResponseDto) {
        if (!kakaoAuthService.existsMemberByKakaoId(kakaoId)) {
            kakaoAuthService.join(kakaoUserInfoDto)
        }
    }

    private fun login(
        kakaoTokenDto: KakaoTokenResponseDto, kakaoId: Long,
        response: HttpServletResponse, headers: HttpHeaders
    ): ResponseEntity<Any> {
        val loginDto = kakaoAuthService.login(kakaoId, kakaoTokenDto)

        cookieService.addAccessTokenToCookie(loginDto.accessToken, response)
        cookieService.addRefreshTokenToCookieWithSameSiteNone(loginDto.refreshToken, response)

        return ResponseEntity.status(HttpStatus.FOUND)
            .headers(headers)
            .body(ApiResponse.of<Any>("성공적으로 로그인 되었습니다. nickname : " + loginDto.nickname))
    }

    /**
     * 카카오 로그아웃 옵션 선택 페이지로 리다이렉트
     * @param
     * @return
     */
    @GetMapping("/logout")
    fun kakaoLogout(): ResponseEntity<Void> {
        val headers = HttpHeaders()
        headers.location = URI.create(kakaoAuthService.getKakaoLogoutUrl())

        return ResponseEntity.status(HttpStatus.FOUND)
            .headers(headers).body(null)
    }

    @GetMapping("/logout/callback")
    fun handleKakaoLogoutCallback(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse<String>> {

        val refreshToken = cookieService.getRefreshTokenFromCookie(request)
        kakaoAuthService.logout(refreshToken)
        cookieService.clearTokenFromCookie(response)

        val headers = HttpHeaders()
        headers.location = URI.create(clientBaseUrl)

        return ResponseEntity.status(HttpStatus.FOUND)
            .headers(headers).body(ApiResponse.of("성공적으로 로그아웃 되었습니다."))
    }
}
