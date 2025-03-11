package com.example.backend.global.response

/**
 * ApiResponse
 * 공통 응답 객체
 * @author 100minha
 */
data class ApiResponse<T>(
    val message: String,
    val data: T? = null
) {

    constructor(data: T) : this(message = "Success", data = data)
}
