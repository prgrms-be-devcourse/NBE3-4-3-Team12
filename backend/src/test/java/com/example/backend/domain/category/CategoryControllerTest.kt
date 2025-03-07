package com.example.backend.test.category

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.repository.AdminRepository
import com.example.backend.domain.category.controller.CategoryController
import com.example.backend.domain.category.dto.CategoryRequestDto
import com.example.backend.domain.category.dto.CategoryResponseDto
import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import com.example.backend.domain.category.repository.CategoryRepository
import com.example.backend.domain.category.service.CategoryService
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.global.util.TestTokenProvider
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
import java.util.*

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
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var categoryService: CategoryService
    @Autowired
    private lateinit var tokenProvider: TestTokenProvider
    @Autowired
    private lateinit var categoryRepository: CategoryRepository
    @Autowired
    private lateinit var mvc: MockMvc
    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var accessToken: String

    private fun loginAndGetResponse(): ResultActions {
        val loginRequestJson = """
            {
                "adminName": "admin",
                "password": "1234"
            }
        """.trimIndent()

        return mockMvc.perform(
            post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestJson)
        )
    }

    @BeforeEach
    fun setUp() {
        val admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
        adminRepository.save(admin)
        memberRepository.deleteAll()

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
        val loginResponse = loginAndGetResponse()
        loginResponse.andExpect(status().isOk)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        assertNotNull(accessToken)
        assertNotNull(refreshToken)

        val resultActions: ResultActions = mvc.perform(
            post("/categories")
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
                .content("""
            {
                "type": "HOBBY",
                "name": "새로운 취미 카테고리"
            }
        """)
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
        val resultActions: ResultActions = mvc.perform(
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
        val resultActions: ResultActions = mvc.perform(
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
        val loginResponse = loginAndGetResponse()
        loginResponse.andExpect(status().isOk)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        assertNotNull(accessToken)
        assertNotNull(refreshToken)

        val resultActions: ResultActions = mvc.perform(
            put("/categories/{id}", 1L)
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
                .content("""
                    {
                        "type": "EXERCISE",
                        "name": "수정된 카테고리"
                    }
                """)
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(CategoryController::class.java))
            .andExpect(handler().methodName("modifyCategory"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.type").value("EXERCISE"))
            .andExpect(jsonPath("$.name").value("수정된 카테고리"))
    }
}
