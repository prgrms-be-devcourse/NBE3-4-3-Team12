package com.example.backend.domain.category.dto

import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType

data class CategoryResponseDto(
    val id: Long?,
    val type: CategoryType,
    val name: String
) {
    constructor(category: Category) : this(
        id = category.id,
        type = category.categoryType,
        name = category.name
    )
}
