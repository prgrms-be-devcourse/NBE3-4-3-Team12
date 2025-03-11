package com.example.backend.domain.vote.controller

import com.example.backend.domain.vote.dto.VoteRequestDto
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.global.util.TestTokenProvider
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest // 실제환경과 동일하게 테스트 수행
@AutoConfigureMockMvc // MockMVC 자동 구성해서 HTTP 모의 요청
@ActiveProfiles("test") //test 프로필 application-test.yml 사용 설정
@Transactional // 테스트 메서드 실행후 DB 변경사항 롤백
class VoteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc //mockMvc: HTTP 요청을 모의로 보내고 응답을 검증하는 데 사용

    @Autowired
    private lateinit var objectMapper: ObjectMapper // 객체를 JSON 직렬화 or JSON을 객체로 역직렬화 도구

    //    @Autowired
//    private lateinit var voteService: VoteService
    @Autowired
    private lateinit var voteRepository: VoteRepository

    @Autowired
    private lateinit var tokenProvider: TestTokenProvider // 테스트용 토큰 생성용도

    @PersistenceContext
    private lateinit var em: EntityManager // DB 직접 상호작용

    private lateinit var accessToken: String
    private lateinit var testVote: Vote
    private lateinit var voteRequestDto: VoteRequestDto

    @BeforeEach // 각 테스트 메서드가 실행되기 전에 매번 실행
    fun setUp() {
        // 테스트 데이터 초기화 및 DB 재설정
        em.createNativeQuery("ALTER TABLE vote ALTER COLUMN id RESTART WITH 1").executeUpdate()

        // 테스트 사용자 인증 토큰 생성
        accessToken = tokenProvider.generateMemberAccessToken(
            1L, "testUser", "test@test.com"
        )

        // 테스트용 투표 요청 DTO 생성
        voteRequestDto = VoteRequestDto(
            location = "테스트 장소",
            address = "테스트 주소",
            latitude = 37.1234,
            longitude = 127.1234
        )

        // 테스트용 투표 데이터 저장
        testVote = voteRequestDto.toEntity(1L) // groupId = 1
        voteRepository.save(testVote)
    }

    @Test
    fun `투표 생성 테스트 (인증 성공)`() {
        // given
        val groupId = 1L
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            post("/votes/groups/$groupId/votes")
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequestDto))
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.location").value(voteRequestDto.location))
            .andExpect(jsonPath("$.address").value(voteRequestDto.address))
            .andExpect(jsonPath("$.latitude").value(voteRequestDto.latitude))
            .andExpect(jsonPath("$.longitude").value(voteRequestDto.longitude))
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    fun `투표 생성 테스트2 (Gemini 테스트용)`() {
        // given
        val groupId = 2L
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            post("/votes/groups/$groupId/votes")
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequestDto))
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.location").value(voteRequestDto.location))
            .andExpect(jsonPath("$.address").value(voteRequestDto.address))
            .andExpect(jsonPath("$.latitude").value(voteRequestDto.latitude))
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    fun `투표 생성 실패 테스트(인증없음)`() {
        // given
        val groupId = 1L

        // when 위와 비교 : accesstoken 없음
        val resultActions = mockMvc.perform(
            post("/votes/groups/$groupId/votes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequestDto))
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("인증에 실패했습니다."))
            .andExpect(jsonPath("$.code").value("401-1"))
    }

    @Test
    fun `그룹별 투표 목록 조회 테스트`() {
        // given
        val groupId = 1L
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            get("/votes/groups/$groupId/votes")
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].location").value(testVote.location))
            .andExpect(jsonPath("$[0].address").value(testVote.address))
    }

    @Test
    fun `특정 투표 조회 테스트`() {
        // given
        val groupId = 1L
        val voteId = testVote.id!!
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            get("/votes/groups/$groupId/votes/$voteId")
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.location").value(testVote.location))
            .andExpect(jsonPath("$.address").value(testVote.address))
    }

    @Test
    fun `투표 수정 테스트`() {
        // given
        val groupId = 1L
        val voteId = testVote.id!!
        val accessTokenCookie = Cookie("accessToken", accessToken)

        val updateRequestDto = VoteRequestDto(
            location = "수정된 장소",
            address = "수정된 주소",
            latitude = 38.5678,
            longitude = 128.5678
        )

        // when
        val resultActions = mockMvc.perform(
            put("/votes/groups/$groupId/votes/$voteId")
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto))
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.location").value(updateRequestDto.location))
            .andExpect(jsonPath("$.address").value(updateRequestDto.address))
            .andExpect(jsonPath("$.latitude").value(updateRequestDto.latitude))
            .andExpect(jsonPath("$.longitude").value(updateRequestDto.longitude))
    }

    @Test
    fun `투표 삭제 테스트`() {
        // given
        val groupId = 1L
        val voteId = testVote.id!!
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            delete("/votes/groups/$groupId/votes/$voteId")
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)

        // 삭제된 투표가 존재하지 않는지 확인
        assert(!voteRepository.existsById(voteId))
    }

    @Test
    fun `가장 많이 투표된 위치 조회 테스트`() {
        // given
        val groupId = 1L
        val accessTokenCookie = Cookie("accessToken", accessToken)

        // when
        val resultActions = mockMvc.perform(
            get("/votes/groups/$groupId/most-voted")
                .cookie(accessTokenCookie)
        )

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mostVotedLocations").isArray)
    }
}