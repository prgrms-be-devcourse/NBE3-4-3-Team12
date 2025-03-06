package com.example.backend.domain.admin

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.exception.AdminErrorCode
import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.admin.repository.AdminRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminRepositoryTest(
    private val mockMvc: MockMvc,
    private val adminRepository: AdminRepository
) {

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

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp(@Autowired adminRepository: AdminRepository) {
            val admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
            adminRepository.save(admin)
        }
    }

    @Test
    @DisplayName("save() 검증 테스트")
    fun save() {
        val admin = adminRepository.findById(1)
            .orElseThrow{AdminException(AdminErrorCode.NOT_FOUND_ADMIN)}

        assertThat(admin.adminName).isEqualTo("admin")
    }

    @Test
    @DisplayName("findByAdminName() 성공 테스트")
    fun findByAdminName() {
        val admin = adminRepository.findByAdminName("admin")
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)

        assertThat(admin.adminName).isEqualTo("admin")
    }

    @Test
    @DisplayName("findByAdminName() 실패 테스트")
    fun findByAdminName_fail() {
        val exception = assertThrows<AdminException> {
            adminRepository.findByAdminName("not_exist")
                ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)
        }

        assertThat(exception.status).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(exception.code).isEqualTo("404")
        assertThat(exception.message).isEqualTo("존재하지 않는 관리자 입니다.")
    }

    @Test
    @DisplayName("findByRefreshToken 검증")
    fun findByRefreshToken() {
        val loginResponse = loginAndGetResponse()
        val response = loginResponse.andReturn().response

        val cookies = response.cookies

        // refreshToken 쿠키 찾기
        val refreshToken = cookies.find { it.name == "refreshToken" }?.value
            ?: throw RuntimeException("Refresh Token not found")

        val admin = adminRepository.findByRefreshToken(refreshToken)
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)

        assertThat(admin.adminName).isEqualTo("admin")
    }
}