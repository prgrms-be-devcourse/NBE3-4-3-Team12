package com.example.backend.domain.category.dto

import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import com.fasterxml.jackson.annotation.JsonProperty

data class CategoryResponseDto(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("type") val type: CategoryType,
    @JsonProperty("name") val name: String
) {
    constructor(category: Category) : this(
        id = category.id,
        type = category.categoryType,
        name = category.name
    )
}
