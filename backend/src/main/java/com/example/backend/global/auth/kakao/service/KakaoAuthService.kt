package com.example.backend.global.auth.kakao.service

import com.example.backend.domain.member.dto.MemberTokenReissueDto
import com.example.backend.domain.member.service.MemberService
import com.example.backend.global.auth.kakao.dto.KakaoTokenResponseDto
import com.example.backend.global.auth.kakao.dto.KakaoUserInfoResponseDto
import com.example.backend.global.auth.kakao.dto.LoginResponseDto
import com.example.backend.global.auth.kakao.exception.KakaoAuthErrorCode
import com.example.backend.global.auth.kakao.exception.KakaoAuthException
import com.example.backend.global.auth.kakao.util.KakaoAuthUtil
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.auth.util.TokenProvider
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * KakaoAuthService
 * 카카오 로그인 관련 비즈니스 로직을 처리하는 서비스 클래스
 * @author 100minha
 */
@RequiredArgsConstructor
@Service
class KakaoAuthService(
    private val kakaoAuthUtil: KakaoAuthUtil,
    private val webClient: WebClient,
    private val tokenProvider: TokenProvider,
    private val memberService: MemberService,
    private val cookieService: CookieService
) {

    fun getKakaoAuthorizationUrl(): String = kakaoAuthUtil.getKakaoAuthorizationUrl()

    /**
     * 카카오 서버에 엑세스 토큰, 리프레시 토큰 발급을 요청하는 메서드
     * @param authorizationCode
     * @return
     */
    fun getTokenFromKakao(authorizationCode: String): KakaoTokenResponseDto {

        return webClient.post()
            .uri(kakaoAuthUtil.getKakaoLoginTokenUrl(authorizationCode))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.INVALID_PARAMETER))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.KAKAO_SERVER_ERROR))
            }
            .bodyToMono(KakaoTokenResponseDto::class.java)
            .block() ?: throw KakaoAuthException(KakaoAuthErrorCode.KAKAO_SERVER_ERROR)
    }

    /**
     * 카카오 서버에 엑세스 토큰을 사용하여 사용자 정보를 요청하는 메서드
     * @param accessToken
     * @return
     */
    fun getUserInfo(accessToken: String): KakaoUserInfoResponseDto {

        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        headers.contentType = MediaType.valueOf("application/x-www-form-urlencoded;charset=utf-8")

        val kakaoUserInfoDto = webClient.get()
            .uri(kakaoAuthUtil.getUserInfoUrl())
            .headers { it.addAll(headers) }
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.INVALID_PARAMETER))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.KAKAO_SERVER_ERROR))
            }
            .bodyToMono(KakaoUserInfoResponseDto::class.java)
            .block() ?: throw KakaoAuthException(KakaoAuthErrorCode.KAKAO_SERVER_ERROR)

        return kakaoUserInfoDto
    }

    @Transactional
    fun login(kakaoId: Long, kakaoTokenDto: KakaoTokenResponseDto): LoginResponseDto {

        val member = memberService.findByKakaoId(kakaoId)
        member.updateAccessToken(kakaoTokenDto.accessToken)
        member.updateRefreshToken(kakaoTokenDto.refreshToken!!)

        val accessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )

        return LoginResponseDto(member.nickname, accessToken, kakaoTokenDto.refreshToken)
    }

    fun getKakaoLogoutUrl(userId: Long): String =
        kakaoAuthUtil.getLogoutUrl(userId)

    fun existsMemberByKakaoId(kakaoId: Long): Boolean =
        memberService.existsByKakaoId(kakaoId)


    fun join(kakaoUserInfoDto: KakaoUserInfoResponseDto) {
        memberService.join(kakaoUserInfoDto)
    }

    @Transactional
    fun logout(userId: Long, response: HttpServletResponse) {
        cookieService.clearTokenFromCookie(response)
        val member = memberService.findById(userId)
        member.updateAccessToken("")
        member.updateRefreshToken("")

        SecurityContextHolder.clearContext()
    }

    @Transactional
    fun reissueTokens(refreshToken: String): MemberTokenReissueDto {
        val member = memberService.findByKakaoRefreshToken(refreshToken)

        val headers = HttpHeaders()
        headers.contentType = MediaType.valueOf("application/x-www-form-urlencoded;charset=utf-8")

        val kakaoTokenDto = webClient.post()
            .uri(kakaoAuthUtil.getKakaoTokenReissueUrl(refreshToken))
            .headers { it.addAll(headers) }
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.INVALID_PARAMETER))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                Mono.error(KakaoAuthException(KakaoAuthErrorCode.KAKAO_SERVER_ERROR))
            }
            .bodyToMono(KakaoTokenResponseDto::class.java)
            .block()

        // dto에 토큰정보가 담겨있지 않을 시 예외 반환 및 리프레시 토큰은 body에 있을 경우에만 갱신
        if (kakaoTokenDto != null) {
            member.updateAccessToken(kakaoTokenDto.accessToken)
            if (kakaoTokenDto.refreshToken != null) {
                member.updateRefreshToken(kakaoTokenDto.refreshToken)
            }
        } else {
            throw KakaoAuthException(KakaoAuthErrorCode.TOKEN_REISSUE_FAILED)
        }

        return MemberTokenReissueDto.of(member)
    }
}
