package com.example.backend.domain.admin.controller

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.repository.AdminRepository
import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.entity.GroupStatus
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.global.redis.service.RedisService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminControllerTest(
    private val mockMvc: MockMvc,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val adminRepository: AdminRepository,
    private val redisService: RedisService
) {

    @PersistenceContext
    private lateinit var em: EntityManager

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
        val member = Member(100L, "testUser", "test@test.com")
        memberRepository.save(member)

        val group = Group("title", "description", member, GroupStatus.RECRUITING, 10)
        return groupRepository.save(group).id!!
    }

    @BeforeEach
    fun setUp() {
        em.createNativeQuery("ALTER TABLE admin ALTER COLUMN id RESTART WITH 1").executeUpdate()
        val admin = Admin("admin", "\$2a\$12\$wS8w9vGzZ345XlGazbp8mekCkPyKoPFbky96pr0EqW.6I0Xtdt.YO")
        adminRepository.save(admin)
    }


    @Test
    @DisplayName("관리자 로그인 테스트")
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
    @DisplayName("관리자 로그아웃 테스트")
    fun logoutSuccessTest() {
        val loginResponse = loginAndGetResponse()

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
    @DisplayName("그룹 삭제 테스트")
    fun deleteAdminTest() {
        val groupId = createGroupResponse()

        val loginResponse = loginAndGetResponse()

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

    @Test
    @DisplayName("유저 블랙리스트 테스트")
    fun blacklistedMember() {
        val loginResponse = loginAndGetResponse()
        redisService.save("refreshToken", "kakao: 1", 3L)

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        val deleteGroupResponse = mockMvc.perform(
            delete("/admin/members/1/blacklist")
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
        )

        deleteGroupResponse
            .andExpect(status().isNoContent)

        val response = redisService.get("refreshToken")
        assertThat(response).isEqualTo("blacklisted")
    }

    @Test
    @DisplayName("유저 검색 테스트")
    fun searchMemberTest() {
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate()
        val loginResponse = loginAndGetResponse()

        val accessToken = loginResponse.andReturn().response.getCookie("accessToken")?.value
        val refreshToken = loginResponse.andReturn().response.getCookie("refreshToken")?.value

        for (i in 1 .. 5) {
            memberRepository.save(Member((100 + i).toLong(), "testUser$i", "test$i@test.com"))
            memberRepository.save(Member((200 + i).toLong(), "testUser$i", "test$i@test.com"))
        }

        val searchResponse = mockMvc.perform(
            get("/admin/members/search")
                .param("nickname","testUser1")
                .cookie(Cookie("accessToken", accessToken))
                .cookie(Cookie("refreshToken", refreshToken))
        )

        // 응답 검증
        searchResponse
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.size()").value(2))  // data 배열의 크기 검증 (2개)
            .andExpect(jsonPath("$.data[0].id").value(1))  // 첫 번째 유저의 id 검증
            .andExpect(jsonPath("$.data[0].nickname").value("testUser1"))  // 첫 번째 유저의 nickname 검증
            .andExpect(jsonPath("$.data[0].email").value("test1@test.com"))  // 첫 번째 유저의 email 검증
            .andExpect(jsonPath("$.data[1].id").value(2))  // 두 번째 유저의 id 검증
            .andExpect(jsonPath("$.data[1].nickname").value("testUser1"))  // 두 번째 유저의 nickname 검증
            .andExpect(jsonPath("$.data[1].email").value("test1@test.com"))  // 두 번째 유저의 email 검증
    }
}
