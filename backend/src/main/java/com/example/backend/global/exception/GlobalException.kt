package com.example.backend.global.exception

import org.springframework.http.HttpStatus

/**
 * GlobalException
 * 전역에서 발생할 수 있는 커스텀 예외 처리 클래스
 * @author 100minha
 */
class GlobalException(private val globalErrorCode: GlobalErrorCode) :
    RuntimeException() {

    override val message: String
        get() = globalErrorCode.message

    val status: HttpStatus
        get() = globalErrorCode.httpStatus

    val code: String
        get() = globalErrorCode.code
}
