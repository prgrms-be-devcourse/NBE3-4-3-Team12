package com.example.backend.domain.voter.controller

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.entity.GroupStatus
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupmember.entity.GroupMember
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.global.redis.service.RedisService
import com.example.backend.global.util.TestTokenProvider
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VoterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var voteRepository: VoteRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var groupMemberRepository: GroupMemberRepository

    @Autowired
    private lateinit var tokenProvider: TestTokenProvider

    @MockitoBean
    private lateinit var redisService: RedisService

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var group: Group
    private lateinit var vote: Vote
    private lateinit var member: Member
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    @BeforeEach // 각 테스트 메서드가 실행되기 전에 매번 실행
    fun setUp() {

        // 테스트 데이터 초기화 및 DB 재설정
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate()
        em.createNativeQuery("ALTER TABLE \"groups\" ALTER COLUMN id RESTART WITH 1").executeUpdate()

        // 테스트에 사용할 멤버 생성 및 저장
        member = memberRepository.save(
            Member(email = "test@example.com", nickname = "테스트 유저", kakaoId = 123456789)
        )

        // 멤버가 포함된 그룹을 생성 및 저장
        group = groupRepository.save(
            Group(
                title = "테스트 그룹",
                description = "테스트 그룹 설명",
                maxParticipants = 10,
                status = GroupStatus.RECRUITING,
                member = member
            )
        )


        // 그룹에 멤버 추가
        groupMemberRepository.save(GroupMember(group = group, member = member))


        // 특정 그룹과 연결된 투표를 생성하여 저장
        vote = voteRepository.save(
            Vote(
                groupId = group.id!!,
                location = "테스트 장소",
                address = "테스트 주소",
                latitude = 37.1234,
                longitude = 127.1234
            )
        )

        // 테스트 요청 시 필요한 인증을 위해 JWT 액세스 토큰 생성
        accessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )

        // 테스트 요청 시 필요한 인증을 위해 리프레시 토큰 생성
        refreshToken = tokenProvider.generateMemberRefreshToken()

        `when`(redisService.isValidRefreshToken(Mockito.anyString())).thenReturn(true)
    }

    @Test
    @DisplayName("투표자 등록 성공")
    fun addVoter_Success() {
        // JWT 인증을 위한 쿠키 생성
        val accessTokenCookie = Cookie("accessToken", accessToken)
        val refreshTokenCookie = Cookie("refreshToken", refreshToken)

        // 투표자 등록 요청을 실행하고 200 OK 응답을 기대
        mockMvc.perform(
            post("/voters/${group.id}/${vote.id}")
                .cookie(accessTokenCookie) // 인증 쿠키 추가
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("투표자 조회 성공")
    fun getVotersByVote_Success() {
        // JWT 인증을 위한 쿠키 생성
        val accessTokenCookie = Cookie("accessToken", accessToken)
        val refreshTokenCookie = Cookie("refreshToken", refreshToken)

        // 테스트를 위해 투표자 추가
        mockMvc.perform(
            post("/voters/${group.id}/${vote.id}")
                .cookie(accessTokenCookie)
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)

        // 투표에 참여한 멤버 목록을 조회하고 200 OK 응답을 기대
        mockMvc.perform(
            get("/voters/${vote.id}")
                .cookie(accessTokenCookie)
                .cookie(refreshTokenCookie)
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("투표자 삭제 성공")
    fun removeVoter_Success() {
        // JWT 인증을 위한 쿠키 생성
        val accessTokenCookie = Cookie("accessToken", accessToken)
        val refreshTokenCookie = Cookie("refreshToken", refreshToken)

        // 삭제 테스트 전에 투표자를 먼저 등록하여 상태를 세팅
        mockMvc.perform(
            post("/voters/${group.id}/${vote.id}")
                .cookie(accessTokenCookie)
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)  // 정상적으로 추가되었는지 확인

        // 등록된 투표자를 삭제 요청하여 204 No Content 응답을 기대
        mockMvc.perform(
            delete("/voters/${group.id}/${vote.id}")
                .cookie(accessTokenCookie) // 인증 쿠키 추가
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }
}
