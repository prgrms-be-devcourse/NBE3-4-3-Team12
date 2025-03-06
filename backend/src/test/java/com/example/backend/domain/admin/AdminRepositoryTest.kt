package com.example.backend.domain.admin

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.exception.AdminErrorCode
import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.admin.repository.AdminRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminRepositoryTest(
    private val adminRepository: AdminRepository
) {

    private lateinit var admin: Admin
    private val refeshToken = "468cb628-5f71-4e34-89fb-7e5b6ce7575b"

        @BeforeAll
        fun init() {
            admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
            admin.setRefreshToken("468cb628-5f71-4e34-89fb-7e5b6ce7575b", LocalDateTime.now())
            adminRepository.save(admin)
        }

    @Test
    @DisplayName("save() 검증 테스트")
    fun save() {
        val admin = adminRepository.findById(1)
            .orElseThrow{AdminException(AdminErrorCode.NOT_FOUND_ADMIN)}
        assertThat(admin.adminName).isEqualTo("admin")
        assertThat(admin.refreshToken).isEqualTo(refeshToken)
    }

    @Test
    @DisplayName("findByAdminName() 성공 테스트")
    fun findByAdminName() {
        val admin = adminRepository.findByAdminName("admin")
        assertThat(admin!!.adminName).isEqualTo("admin")
        assertThat(admin.refreshToken).isEqualTo(refeshToken)
    }

    @Test
    @DisplayName("findByAdminName() 실패 테스트")
    fun findByAdminName_fail() {
        val admin = adminRepository.findByAdminName("not_exist")
        assertThat(admin).isNull()
    }

    @Test
    @DisplayName("findByRefreshToken 검증")
    fun findByRefreshToken() {
        val admin = adminRepository.findByRefreshToken(refeshToken)
        assertThat(admin!!.adminName).isEqualTo("admin")
        assertThat(admin.refreshToken).isEqualTo(refeshToken)
    }

    @Test
    @DisplayName("findByRefreshToken 실패 테스트")
    fun findByRefreshToke_fail() {
        val admin = adminRepository.findByRefreshToken("")
        assertThat(admin).isNull()
    }
}