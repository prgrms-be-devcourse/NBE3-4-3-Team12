package com.example.backend.global.auth.kakao.service

import com.example.backend.domain.member.dto.MemberTokenReissueDto
import com.example.backend.domain.member.service.MemberService
import com.example.backend.global.auth.kakao.dto.KakaoTokenResponseDto
import com.example.backend.global.auth.kakao.dto.KakaoUserInfoResponseDto
import com.example.backend.global.auth.kakao.dto.LoginResponseDto
import com.example.backend.global.auth.kakao.exception.KakaoAuthErrorCode
import com.example.backend.global.auth.kakao.exception.KakaoAuthException
import com.example.backend.global.auth.kakao.util.KakaoAuthUtil
import com.example.backend.global.auth.util.JwtUtil
import com.example.backend.global.auth.util.TokenProvider
import com.example.backend.global.redis.service.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

/**
 * KakaoAuthService
 * 카카오 로그인 관련 비즈니스 로직을 처리하는 서비스 클래스
 * @author 100minha
 */
@Service
class KakaoAuthService(
    private val kakaoAuthUtil: KakaoAuthUtil,
    private val webClient: WebClient,
    private val jwtUtil: JwtUtil,
    private val tokenProvider: TokenProvider,
    private val memberService: MemberService,
    private val redisService: RedisService,
    @Value("\${spring.security.jwt.refresh-token.expiration}")
    private val refreshTokenExpirationTime: Long
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
        val refreshToken = kakaoTokenDto.refreshToken
            ?: throw KakaoAuthException(KakaoAuthErrorCode.TOKEN_NOT_FOUND)

        saveRefreshToken(refreshToken, member.id!!)

        val accessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )

        return LoginResponseDto(member.nickname, accessToken, kakaoTokenDto.refreshToken)
    }

    fun getKakaoLogoutUrl(): String =
        kakaoAuthUtil.getLogoutUrl()

    fun existsMemberByKakaoId(kakaoId: Long): Boolean =
        memberService.existsByKakaoId(kakaoId)

    fun join(kakaoUserInfoDto: KakaoUserInfoResponseDto) {
        memberService.join(kakaoUserInfoDto)
    }

    @Transactional
    fun logout(refreshToken: String?) {
        // 리프레시 토큰이 존재하면 삭제
        refreshToken?.let { redisService.addBlackList(refreshToken.toString(), jwtUtil.getRefreshTokenExpirationTime()) }

        SecurityContextHolder.clearContext()
    }

    @Transactional
    fun reissueTokens(refreshToken: String): MemberTokenReissueDto {

        val rawKakaoMemberId = redisService.get(refreshToken)
            ?: throw KakaoAuthException(KakaoAuthErrorCode.TOKEN_REISSUE_FAILED)

        val kakaoMemberId = rawKakaoMemberId
            .substringAfter("kakao: ").trim().toLong()

        val memberInfoDto = memberService.findMemberInfoDtoById(kakaoMemberId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.valueOf("application/x-www-form-urlencoded;charset=utf-8")

        // 카카오 서버에 리프레시 토큰을 사용하여 새로운 엑세스 토큰을 요청
        // 현재 리프레시 토큰이 유효한 상태인지 검증
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
            .block() ?: throw KakaoAuthException(KakaoAuthErrorCode.TOKEN_REISSUE_FAILED)

        // 리프레시 토큰은 body에 있을 경우에만 갱신
        if (kakaoTokenDto.refreshToken != null) {
            redisService.addBlackList(refreshToken, jwtUtil.getRefreshTokenExpirationTime())
            saveRefreshToken(kakaoTokenDto.refreshToken, kakaoMemberId)

            return MemberTokenReissueDto(memberInfoDto, kakaoTokenDto.refreshToken)
        }

        return MemberTokenReissueDto(memberInfoDto, refreshToken)
    }

    private fun saveRefreshToken(refreshToken: String, kakaoMemberId: Long) {
        redisService.save(
            refreshToken,
            "kakao: $kakaoMemberId",
            TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpirationTime)
        )
    }
}
