package com.example.backend.global.auth.kakao.exception

import org.springframework.http.HttpStatus

/**
 * KakaoAuthException
 * 카카오와 통신 도중 발생할 수 있는 예외 처리
 * @author 100minha
 */
class KakaoAuthException(private val kakaoAuthErrorCode: KakaoAuthErrorCode) :
    RuntimeException() {

    override val message: String
        get() = kakaoAuthErrorCode.message

    val status: HttpStatus
        get() = kakaoAuthErrorCode.httpStatus

    val code: String
        get() = kakaoAuthErrorCode.code
}
