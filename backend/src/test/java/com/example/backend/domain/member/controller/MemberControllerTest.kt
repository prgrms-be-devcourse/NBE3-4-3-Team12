package com.example.backend.domain.member.controller

import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.global.util.TestTokenProvider
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

/**
 * MemberControllerTest
 *
 *
 * @author 100minha
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenProvider: TestTokenProvider

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate()

        val member = Member(1L, "testUser", "test@test.com")
        memberRepository.save(member)

        accessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )
    }

    @Test
    @DisplayName("로그인된 사용자 정보 조회 테스트")
    fun getCurrentMemberTest() {
        // given
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(get("/members").cookie(accessTokenCookie))

        // then
        resultActions
            .andExpect(handler().handlerType(MemberController::class.java))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.nickname").value("testUser"))
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
    }

    @Test
    @DisplayName("로그인 되지 않은 사용자 정보 조회 실패 테스트")
    fun getCurrentMemberWhenNoCookieTest() {
        // when
        val resultActions = mockMvc.perform(get("/members"))

        // then
        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("인증에 실패했습니다."))
            .andExpect(jsonPath("$.code").value("401-1"))
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 테스트")
    fun modifyTest() {
        // given
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            put("/members")
                .content(
                    """
                    {
                        "nickname" : "modified"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andExpect(handler().handlerType(MemberController::class.java))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.nickname").value("modified"))
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
    }

    @Test
    @DisplayName("사용자 정보 수정 실패(nickname 공백) 테스트")
    fun modifyFailWhenBlankNicknameTest() {
        // given
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            put("/members")
                .content(
                    """
                    {
                        "nickname" : ""
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andExpect(handler().handlerType(MemberController::class.java))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("입력된 객체가 유효하지 않습니다"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.errors[0].field").value("nickname"))
            .andExpect(jsonPath("$.errors[0].message").value("닉네임은 최소 2글자 이상, 10글자 이하이어야 합니다."))
    }

    @Test
    @DisplayName("사용자 정보 수정 실패(nickname 길이 최대치 초과) 테스트")
    fun modifyFailWhenTooLongNicknameTest() {
        // given
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            put("/members")
                .content(
                    """
                    {
                        "nickname" : "too_Long_Nickname"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andExpect(handler().handlerType(MemberController::class.java))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("입력된 객체가 유효하지 않습니다"))
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.errors[0].field").value("nickname"))
            .andExpect(jsonPath("$.errors[0].message").value("닉네임은 최소 2글자 이상, 10글자 이하이어야 합니다."))
    }

    @Test
    @DisplayName("사용자 정보 수정 실패(로그인되지 않은 상태) 테스트")
    fun modifyFailWhenNoCookieTest() {
        // when
        val resultActions = mockMvc.perform(
            put("/members")
                .content(
                    """
                    {
                        "nickname" : "modified"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("인증에 실패했습니다."))
            .andExpect(jsonPath("$.code").value("401-1"))
    }
}

