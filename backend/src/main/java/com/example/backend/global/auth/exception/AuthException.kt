package com.example.backend.global.auth.exception

import org.springframework.http.HttpStatus

/**
 * AuthException
 * 인증 예외 처리를 위한 커스텀 예외 클래스
 * @author 100mihna
 */
class AuthException(private val authErrorCode: AuthErrorCode) :
    RuntimeException() {

    override val message: String
        get() = authErrorCode.message

    val status: HttpStatus
        get() = authErrorCode.httpStatus

    val code: String
        get() = authErrorCode.code
}