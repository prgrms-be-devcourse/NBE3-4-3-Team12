package com.example.backend.global.exception

import org.springframework.http.HttpStatus

/**
 * GlobalErrorCode
 * 전역에서 발생 할 수 있는 커스텀 예외 정리 클래스
 * @author 100minha
 */

enum class GlobalErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    NOT_VALID(HttpStatus.BAD_REQUEST, "400-1", "입력된 객체가 유효하지 않습니다");

}
