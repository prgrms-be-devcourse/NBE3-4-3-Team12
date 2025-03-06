package com.example.backend.domain.admin

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.repository.AdminRepository
import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.entity.GroupStatus
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminControllerTest(
        private val mockMvc: MockMvc,
        private val groupRepository: GroupRepository,
        private val memberRepository: MemberRepository
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

    private fun createGroupResponse(): Long {
        val member = Member(1L, "testUser", "test@test.com")
        memberRepository.save(member)

        val group = Group("운동모임", "운동 하는 사람들의 모임", member, GroupStatus.RECRUITING, 10)
        return groupRepository.save(group).id!!
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
    @DisplayName("로그인 성공 테스트")
    fun loginSuccessTest() {
        val loginResponse = loginAndGetResponse()

        loginResponse
                .andExpect(status().isOk)
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andReturn()
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    fun logoutSuccessTest() {
        val loginResponse = loginAndGetResponse()

        loginResponse.andExpect(status().isOk)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        assertNotNull(accessToken)
        assertNotNull(refreshToken)

        val logoutResponse = mockMvc.perform(
                post("/admin/logout")
                        .cookie(Cookie("accessToken", accessToken))
                        .cookie(Cookie("refreshToken", refreshToken))
        )

        logoutResponse
                .andExpect(status().isOk)
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""))
    }

    @Test
    @DisplayName("관리자 그룹 삭제 테스트")
    fun deleteAdminTest() {
        val groupId = createGroupResponse()

        val loginResponse = loginAndGetResponse()
        loginResponse.andExpect(status().isOk)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        assertNotNull(accessToken)
        assertNotNull(refreshToken)

        val deleteGroupResponse = mockMvc.perform(
                delete("/admin/group/$groupId")
                        .cookie(Cookie("accessToken", accessToken))
                        .cookie(Cookie("refreshToken", refreshToken))
        )

        deleteGroupResponse.andExpect(status().isNoContent)

        val group = groupRepository.findById(groupId).orElseThrow()
        assert(group.status == GroupStatus.DELETED)
    }
}
