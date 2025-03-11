package com.example.backend.domain.category.service

import com.example.backend.domain.category.dto.CategoryRequestDto
import com.example.backend.domain.category.dto.CategoryResponseDto
import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.exception.CategoryErrorCode
import com.example.backend.domain.category.exception.CategoryException
import com.example.backend.domain.category.repository.CategoryRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    @CacheEvict(value = ["categories"], allEntries = true)
    fun create(requestDto: CategoryRequestDto): CategoryResponseDto {
        val category = Category(
            name = requestDto.name,
            categoryType = requestDto.type
        )
        categoryRepository.save(category)
        return CategoryResponseDto(category)
    }

    @Transactional
    @CacheEvict(value = ["categories"], allEntries = true)
    fun modify(id: Long, categoryRequestDto: CategoryRequestDto): CategoryResponseDto {
        val category = categoryRepository.findById(id)
            .orElseThrow { CategoryException(CategoryErrorCode.NOT_FOUND) }

        category.modify(
            categoryRequestDto.name,
            categoryRequestDto.type
        )
        categoryRepository.save(category)
        return CategoryResponseDto(category)
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["categories"])
    fun getAllCategories(): List<CategoryResponseDto> {
        val categories = categoryRepository.findAll()
        if (categories.isEmpty()) {
            throw CategoryException(CategoryErrorCode.NOT_FOUND_LIST)
        }
        return categories.map { CategoryResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getCategory(id: Long): CategoryResponseDto {
        val category = categoryRepository.findById(id)
            .orElseThrow { CategoryException(CategoryErrorCode.NOT_FOUND) }
        return CategoryResponseDto(category)
    }

    @Transactional
    @CacheEvict(value = ["categories"], allEntries = true)
    fun delete(id: Long) {
        val category = categoryRepository.findById(id)
            .orElseThrow { CategoryException(CategoryErrorCode.NOT_FOUND) }
        categoryRepository.delete(category)
    }
}
