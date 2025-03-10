package com.example.backend.domain.category.repository

import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class CategoryRepositoryTest {
    @Autowired
    lateinit var categoryRepository: CategoryRepository

    private lateinit var category: Category


    @BeforeEach
    fun setUp() {
        category = Category(
            name = "Category1",
            categoryType = CategoryType.EXERCISE
        )
    }

    @Test
    @DisplayName("카테고리 저장 테스트")
    fun saveTest() {
        val savedCategory = categoryRepository.save(category)

        assertNotNull(savedCategory.id)
        assertEquals(category.name, savedCategory.name)
        assertEquals(category.categoryType, savedCategory.categoryType)
    }

    @Test
    @DisplayName("카테고리 ID로 조회 테스트")
    fun findByIdTest() {
        val savedCategory = categoryRepository.save(category)
        val foundCategory = categoryRepository.findByIdOrNull(savedCategory.id)

        assertNotNull(foundCategory)
        assertEquals(savedCategory.name, foundCategory?.name)
        assertEquals(savedCategory.categoryType, foundCategory?.categoryType)
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    fun deleteTest() {
        val savedCategory = categoryRepository.save(category)

        categoryRepository.delete(savedCategory)

        val deletedCategory = categoryRepository.findByIdOrNull(savedCategory.id)
        assertTrue(deletedCategory == null)
    }
}
