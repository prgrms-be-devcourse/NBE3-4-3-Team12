package com.example.backend.domain.admin.exception;

import org.springframework.http.HttpStatus;

class AdminException(
    private val adminErrorCode: AdminErrorCode
) : RuntimeException(adminErrorCode.message) {

    override val message: String
        get() = adminErrorCode.message

    val status: HttpStatus
        get() = adminErrorCode.httpStatus

    val code: String
        get() = adminErrorCode.code
}
