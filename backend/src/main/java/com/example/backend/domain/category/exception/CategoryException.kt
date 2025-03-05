package com.example.backend.domain.category.exception

import org.springframework.http.HttpStatus

class CategoryException(val categoryErrorCode: CategoryErrorCode) : RuntimeException(categoryErrorCode.message) {

    override val message: String
        get() = categoryErrorCode.message

    val status: HttpStatus
        get() = categoryErrorCode.httpStatus

    val code: String
        get() = categoryErrorCode.code
}
