package com.example.backend.domain.category.service

import com.example.backend.domain.category.dto.CategoryRequestDto
import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import com.example.backend.domain.category.exception.CategoryException
import com.example.backend.domain.category.repository.CategoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @InjectMocks
    private lateinit var categoryService: CategoryService

    private lateinit var categoryRequestDto: CategoryRequestDto
    private lateinit var category1: Category
    private lateinit var category2: Category

    @BeforeEach
    fun setUp() {
        categoryRequestDto = CategoryRequestDto(
            name = "Test Category",
            type = CategoryType.EXERCISE
        )

        category1 = Category(name = "Category 1", categoryType = CategoryType.EXERCISE)
        category2 = Category(name = "Category 2", categoryType = CategoryType.HOBBY)

        Mockito.lenient().`when`(categoryRepository.findById(category1.id)).thenReturn(Optional.of(category1))
        Mockito.lenient().`when`(categoryRepository.findById(category2.id)).thenReturn(Optional.of(category2))
        Mockito.lenient().`when`(categoryRepository.findAll()).thenReturn(listOf(category1, category2))
    }

    @Test
    @DisplayName("카테고리 생성 테스트")
    fun createCategoryTest() {
        val category = Category(
            name = categoryRequestDto.name,
            categoryType = categoryRequestDto.type
        )

        `when`(categoryRepository.save(Mockito.any(Category::class.java))).thenReturn(category)

        val response = categoryService.create(categoryRequestDto)

        assertThat(response).isNotNull
        assertThat(response.name).isEqualTo(category.name)
        assertThat(response.type).isEqualTo(category.categoryType)

        Mockito.verify(categoryRepository, Mockito.times(1)).save(Mockito.any(Category::class.java))
    }

    @Test
    @DisplayName("카테고리 수정 테스트")
    fun modifyCategoryTest() {
        `when`(categoryRepository.findById(category1.id)).thenReturn(Optional.of(category1))

        val updatedCategoryDto = CategoryRequestDto(name = "Updated Category", type = CategoryType.STUDY)

        val response = categoryService.modify(category1.id, updatedCategoryDto)

        assertThat(response).isNotNull
        assertThat(response.name).isEqualTo(updatedCategoryDto.name)
        assertThat(response.type).isEqualTo(updatedCategoryDto.type)

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(category1.id)
        Mockito.verify(categoryRepository, Mockito.times(1)).save(Mockito.any(Category::class.java))
    }

    @Test
    @DisplayName("카테고리 수정시 잘못된 id로 실패 테스트")
    fun modifyFailureExceptionTest() {
        val wrongId = 100L

        Mockito.`when`(categoryRepository.findById(wrongId)).thenReturn(Optional.empty())

        val exception = org.junit.jupiter.api.assertThrows<CategoryException> {
            categoryService.modify(wrongId, categoryRequestDto)
        }

        assertThat(exception).isNotNull
        assertThat(exception.message).contains("해당 카테고리는 존재하지 않습니다.")

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(wrongId)
    }

    @Test
    @DisplayName("카테고리 전체 조회 성공 테스트")
    fun listCategoryTest() {
        val categories = listOf(category1, category2)
        `when`(categoryRepository.findAll()).thenReturn(categories)

        val response = categoryService.getAllCategories()

        assertThat(response).hasSize(2)
        assertThat(response[0].name).isEqualTo(category1.name)
        assertThat(response[1].name).isEqualTo(category2.name)

        Mockito.verify(categoryRepository, Mockito.times(1)).findAll()
    }

    @Test
    @DisplayName("카테고리 전체 조회 실패 테스트 - 카테고리가 없을 때")
    fun emptyListCategoryTest() {
        `when`(categoryRepository.findAll()).thenReturn(emptyList())

        val exception = org.junit.jupiter.api.assertThrows<CategoryException> {
            categoryService.getAllCategories()
        }

        assertThat(exception).isNotNull
        assertThat(exception.message).contains("카테고리 목록이 존재하지 않습니다.")

        Mockito.verify(categoryRepository, Mockito.times(1)).findAll()
    }

    @Test
    @DisplayName("카테고리 삭제 성공 테스트")
    fun deleteCategoryTest() {
        `when`(categoryRepository.findById(category1.id)).thenReturn(Optional.of(category1))

        categoryService.delete(category1.id)

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(category1.id)
        Mockito.verify(categoryRepository, Mockito.times(1)).delete(category1)
    }

    @Test
    @DisplayName("카테고리 삭제 시 잘못된 id로 실패 테스트")
    fun deleteCategoryFailureTest() {
        `when`(categoryRepository.findById(category1.id)).thenReturn(Optional.empty())

        val exception = org.junit.jupiter.api.assertThrows<CategoryException> {
            categoryService.delete(category1.id)
        }

        assertThat(exception).isNotNull
        assertThat(exception.message).contains("해당 카테고리는 존재하지 않습니다.")

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(category1.id)
        Mockito.verify(categoryRepository, Mockito.times(0)).delete(Mockito.any(Category::class.java))
    }

    @Test
    @DisplayName("카테고리 조회 성공 테스트")
    fun getCategoryTest() {
        `when`(categoryRepository.findById(category1.id)).thenReturn(Optional.of(category1))

        val response = categoryService.getCategory(category1.id)

        assertThat(response).isNotNull
        assertThat(response.name).isEqualTo(category1.name)
        assertThat(response.type).isEqualTo(category1.categoryType)

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(category1.id)
    }

    @Test
    @DisplayName("카테고리 조회 실패 테스트 - 잘못된 id로 조회 시 실패")
    fun getCategoryFailureTest() {
        val wrongId = 100L

        Mockito.`when`(categoryRepository.findById(wrongId)).thenReturn(Optional.empty())

        val exception = org.junit.jupiter.api.assertThrows<CategoryException> {
            categoryService.getCategory(wrongId)
        }

        assertThat(exception).isNotNull
        assertThat(exception.message).contains("해당 카테고리는 존재하지 않습니다.")

        Mockito.verify(categoryRepository, Mockito.times(1)).findById(wrongId)
    }
}
