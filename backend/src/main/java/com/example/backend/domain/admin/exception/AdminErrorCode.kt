package com.example.backend.domain.admin.exception;

import org.springframework.http.HttpStatus

enum class AdminErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    NOT_FOUND_ADMIN(HttpStatus.NOT_FOUND, "404", "존재하지 않는 관리자 입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "401", "비밀번호가 올바르지않습니다.");
}
