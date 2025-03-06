package com.example.backend.global.response

import lombok.AccessLevel
import lombok.Builder

/**
 * ErrorResponse
 * 전역에서 예외 발생 시 응답떄 사용할 클래스
 * @author 100minha
 */
data class ErrorResponse(
    val message: String,
    val code: String,
    val requestUri: String,
    val errors: List<ValidationError>? = null
) {
    /**
     * Validation 예외 발생 시 필드별 예외 정보 관리 클래스
     * @param field
     * @param message
     */
    data class ValidationError(
        val field: String,  // 에러가 발생한 필드명
        val message: String? = "" // 해당 필드의 에러 메시지
    )

    companion object {
        /**
         * Validation 예외 발생 시 응답 때 사용할 팩토리 메서드
         * @param message    // 예외 메세지
         * @param code    // 예외 상태 코드
         * @param errors    // 필드별 에러 메세지
         */
        fun of(message: String, code: String, requestUri: String, errors: List<ValidationError>): ErrorResponse {
            return ErrorResponse(
                message = message,
                code = code,
                requestUri = requestUri,
                errors = errors
            )
        }

        /**
         * 이외에 FieldError를 포함하지 않는 예외 발생 시 사용할 팩토리 메서드
         * @param message    // 예외 메세지
         * @param code    // 예외 상태 코드
         */
        @JvmStatic
		fun of(message: String, code: String, requestUri: String): ErrorResponse {
            return ErrorResponse(
                message = message,
                code = code,
                requestUri = requestUri
            )
        }
    }
}
