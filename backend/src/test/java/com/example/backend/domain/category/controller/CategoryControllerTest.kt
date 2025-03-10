package com.example.backend.domain.category.controller

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.repository.AdminRepository
import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import com.example.backend.domain.category.repository.CategoryRepository
import com.example.backend.domain.category.service.CategoryService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var categoryService: CategoryService

    @PersistenceContext
    private lateinit var em: EntityManager

    private fun loginAndGetTokens(): Pair<String, String> {
        val loginRequestJson = """
            {
                "adminName": "admin",
                "password": "1234"
            }
        """.trimIndent()

        val loginResponse = mockMvc.perform(
            post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestJson)
        ).andDo(print())

        loginResponse.andExpect(status().isOk)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        assertNotNull(accessToken, "Access Token이 null입니다.")
        assertNotNull(refreshToken, "Refresh Token이 null입니다.")

        return Pair(accessToken!!, refreshToken!!)
    }

    @BeforeEach
    fun setUp() {
        val admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
        adminRepository.save(admin)

        em.createNativeQuery("ALTER TABLE \"groups\" ALTER COLUMN id RESTART WITH 1").executeUpdate()
        em.createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate()

        for (i in 0 until 5) {
            val newCategory = Category(name = "Category$i", categoryType = CategoryType.EXERCISE)
            categoryRepository.save(newCategory)
        }
    }

    @Test
    @DisplayName("카테고리 생성")
    fun createCategoryTest() {
        val (accessToken, refreshToken) = loginAndGetTokens()
        val resultActions: ResultActions = mockMvc.perform(
            post("/categories")
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
                .content(
                    """
            {
                "type": "HOBBY",
                "name": "새로운 취미 카테고리"
            }
        """
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.type").value("HOBBY"))
            .andExpect(jsonPath("$.name").value("새로운 취미 카테고리"))
    }

    @Test
    @DisplayName("카테고리 전체 조회")
    fun listCategoriesTest() {
        val resultActions: ResultActions = mockMvc.perform(
            get("/categories")
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(CategoryController::class.java))
            .andExpect(handler().methodName("listCategories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
    }

    @Test
    @DisplayName("카테고리 특정 조회")
    fun getCategoryTest() {
        val resultActions: ResultActions = mockMvc.perform(
            get("/categories/{id}", 1L)
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(CategoryController::class.java))
            .andExpect(handler().methodName("getCategory"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.type").isNotEmpty())
            .andExpect(jsonPath("$.name").isNotEmpty())
    }

    @Test
    @DisplayName("카테고리 수정")
    fun modifyCategoryTest() {
        val (accessToken, refreshToken) = loginAndGetTokens()
        val resultActions: ResultActions = mockMvc.perform(
            put("/categories/{id}", 1L)
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
                .content(
                    """
                    {
                        "type": "EXERCISE",
                        "name": "수정된 카테고리"
                    }
                """
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(CategoryController::class.java))
            .andExpect(handler().methodName("modifyCategory"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.type").value("EXERCISE"))
            .andExpect(jsonPath("$.name").value("수정된 카테고리"))
    }

    @Test
    @DisplayName("카테고리 삭제")
    fun deleteCategoryTest() {
        val (accessToken, refreshToken) = loginAndGetTokens()
        val resultActions: ResultActions = mockMvc.perform(
            delete("/categories/{id}", 1L)
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))

                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(CategoryController::class.java))
            .andExpect(handler().methodName("deleteCategory"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("존재하지 않는 id의 카테고리 조회")
    fun getCategoryNotFoundTest() {
        val resultActions: ResultActions = mockMvc.perform(
            get("/categories/{id}", 100L)
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("카테고리 목록이 없을 때 조회")
    fun getCategoryListEmptyTest() {
        categoryService.delete(1L)  // redis캐시 무효화를 위해 서비스 삭제 메서드 실행
        categoryRepository.deleteAll()

        val resultActions: ResultActions = mockMvc.perform(
            get("/categories")
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(status().isNotFound)
    }
}
