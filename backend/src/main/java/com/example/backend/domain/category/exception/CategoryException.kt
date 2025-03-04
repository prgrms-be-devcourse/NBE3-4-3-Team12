package com.example.backend.domain.category.exception

import org.springframework.http.HttpStatus

class CategoryException(val categoryErrorCode: CategoryErrorCode) : RuntimeException(categoryErrorCode.message) {

    fun getStatus(): HttpStatus = categoryErrorCode.httpStatus

    fun getCode(): String = categoryErrorCode.code
}
