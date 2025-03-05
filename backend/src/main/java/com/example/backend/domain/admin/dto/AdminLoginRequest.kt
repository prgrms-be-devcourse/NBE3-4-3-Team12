package com.example.backend.domain.admin.dto;

import jakarta.validation.constraints.NotBlank

data class AdminLoginRequest (
    @NotBlank(message = "아이디를 적어주세요")
    val adminName: String,

    @NotBlank(message = "비밀번호를 적어주세요")
    val password: String
)
