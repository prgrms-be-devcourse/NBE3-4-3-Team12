package com.example.backend.global.auth.kakao.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * KakaoResponseBodyDto
 *
 *
 * @author 100minha
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoTokenResponseDto(
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: String,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: String?
)
