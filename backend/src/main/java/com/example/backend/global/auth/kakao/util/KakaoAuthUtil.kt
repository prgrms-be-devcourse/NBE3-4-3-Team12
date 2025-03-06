package com.example.backend.global.auth.kakao.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * KakaoAuthUtil
 * 카카오 인증에서 사용하는 Util클래스 ApiKey, RedirectUri등 제어
 * @author 100minha
 */
@Component
class KakaoAuthUtil(
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private val clientId: String,

    @Value("\${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private val redirectUri: String,

    @Value("\${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private val grantType: String,

    @Value("\${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private val authorizationUri: String,

    @Value("\${spring.security.oauth2.client.provider.kakao.token-uri}")
    private val tokenUri: String,

    @Value("\${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private val userInfoUri: String,

    @Value("\${KAKAO_LOGOUT_URL}")
    private val kakaoLogoutUrl: String,

    @Value("\${KAKAO_LOGOUT_REDIRECT_URI}")
    private val kakaoLogoutRedirectUri: String
) {

    fun getKakaoAuthorizationUrl(): String {
        return UriComponentsBuilder.fromUriString(authorizationUri)
            .queryParam("response_type", "code")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .toUriString()
    }

    fun getKakaoLoginTokenUrl(authorizationCode: String): String {
        return UriComponentsBuilder.fromUriString(tokenUri)
            .queryParam("grant_type", grantType)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("code", authorizationCode)
            .toUriString()
    }

    fun getUserInfoUrl(): String {
        return userInfoUri
    }

    fun getLogoutUrl(userId: Long): String {
        return UriComponentsBuilder.fromUriString(kakaoLogoutUrl)
            .queryParam("client_id", clientId)
            .queryParam("logout_redirect_uri", kakaoLogoutRedirectUri)
            .queryParam("state", userId)
            .toUriString()
    }

    fun getKakaoTokenReissueUrl(refreshToken: String): String {
        return UriComponentsBuilder.fromUriString(tokenUri)
            .queryParam("grant_type", "refresh_token")
            .queryParam("client_id", clientId)
            .queryParam("refresh_token", refreshToken)
            .toUriString()
    }
}