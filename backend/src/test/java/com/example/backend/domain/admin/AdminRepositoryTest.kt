package com.example.backend.domain.admin

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.exception.AdminErrorCode
import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.admin.repository.AdminRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminRepositoryTest(
    private val mockMvc: MockMvc,
    private val adminRepository: AdminRepository
) {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp(@Autowired adminRepository: AdminRepository) {
            val admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
            adminRepository.save(admin)
        }
    }

    @Test
    @DisplayName("findByAdminName() 검증")
    fun findByAdminName() {
        val admin = adminRepository.findByAdminName("admin")
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)

        assertThat(admin.adminName).matches("admin")
    }

//    @Test
//    @DisplayName("findByRefreshToken 검증")
//    fun findByRefreshToken() {
//        val admin = adminRepository.findByRefreshToken()
//    }
//
//    @Test
//    @DisplayName("refreshToken 재발급 검증")
//    fun

}