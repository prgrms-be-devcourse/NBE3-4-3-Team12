package com.example.backend.global.auth.kakao.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * KakaoUserInfoResponseDto
 * 카카오에서 응답해준 유저 정보를 담는 dto
 * @author 100minha
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoUserInfoResponseDto(
	@JsonProperty("id")
    val id: Long,
	@JsonProperty("properties")
    val properties: Properties,
	@JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
) {

    data class Properties(
        @JsonProperty("nickname")
        val nickname: String,
        @JsonProperty("profile_image")
        val profileImage: String
    )

    data class KakaoAccount(
        @JsonProperty("email")
        val email: String
    )
}
