package com.example.backend.domain.category.controller

import com.example.backend.domain.category.dto.CategoryRequestDto
import com.example.backend.domain.category.dto.CategoryResponseDto
import com.example.backend.domain.category.service.CategoryService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {
    private val log: Logger = LoggerFactory.getLogger(CategoryController::class.java)

    @PostMapping
    fun createCategory(@RequestBody @Valid categoryRequestDto: CategoryRequestDto): ResponseEntity<CategoryResponseDto> {
        log.info("New category creation requested")
        val response = categoryService.create(categoryRequestDto)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun modifyCategory(
        @PathVariable id: Long,
        @RequestBody @Valid categoryRequestDto: CategoryRequestDto
    ): ResponseEntity<CategoryResponseDto> {
        log.info("Modify category requested")
        val response = categoryService.modify(id, categoryRequestDto)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun listCategories(): ResponseEntity<List<CategoryResponseDto>> {
        log.info("List categories requested")
        val categoryList = categoryService.getAllCategories()
        return ResponseEntity.ok(categoryList)
    }

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): ResponseEntity<CategoryResponseDto> {
        log.info("Get category requested")
        val response = categoryService.getCategory(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        log.info("Delete category requested")
        categoryService.delete(id)
        return ResponseEntity.ok().build()
    }
}
