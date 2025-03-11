package com.example.backend.global.response

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
}
