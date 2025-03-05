package com.example.backend.domain.admin.exception;

import org.springframework.http.HttpStatus;

class AdminException(
    private val adminErrorCode: AdminErrorCode
) : RuntimeException(adminErrorCode.message) {

    fun getStatus(): HttpStatus {
        return adminErrorCode.httpStatus
    }

    fun getCode(): String {
        return adminErrorCode.code
    }
}
