package com.example.backend.domain.category.exception

import org.springframework.http.HttpStatus

enum class CategoryErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    NOT_FOUND(HttpStatus.NOT_FOUND, "404", "해당 카테고리는 존재하지 않습니다."),
    NOT_FOUND_LIST(HttpStatus.NOT_FOUND, "404", "카테고리 목록이 존재하지 않습니다.")
}
