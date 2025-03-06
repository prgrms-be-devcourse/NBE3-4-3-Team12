package com.example.backend.global.response

import lombok.AccessLevel
import lombok.Builder
import lombok.Getter

/**
 * ApiResponse
 * 공통 응답 객체
 * @author 100minha
 */
data class ApiResponse<T>(
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> of(message: String): ApiResponse<T> {
            return ApiResponse(message = message)
        }

        fun <T> of(data: T): ApiResponse<T> {
            return ApiResponse(message = "Success", data = data)
        }

        fun <T> of(message: String, data: T): ApiResponse<T> {
            return ApiResponse(message = message, data = data)
        }
    }
}
