package com.example.backend.global.auth.kakao.dto

/**
 * LoginResponseDto
 * @author 100minha
 */
data class LoginResponseDto(
    val nickname: String,
    val accessToken: String,
    val refreshToken: String
)
