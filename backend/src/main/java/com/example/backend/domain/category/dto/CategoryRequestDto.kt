package com.example.backend.domain.category.dto

import com.example.backend.domain.category.entity.CategoryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import lombok.AllArgsConstructor
import lombok.Getter

data class CategoryRequestDto(
    @field:NotNull(message = "타입은 필수 항목입니다.")
    val type: CategoryType,

    @field:NotBlank(message = "이름은 필수 항목입니다.")
    val name: String
)