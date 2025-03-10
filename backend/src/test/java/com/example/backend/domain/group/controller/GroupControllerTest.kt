package com.example.backend.test.group;

import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.category.entity.CategoryType
import com.example.backend.domain.category.repository.CategoryRepository
import com.example.backend.domain.group.controller.GroupController
import com.example.backend.domain.group.dto.GroupRequestDto
import com.example.backend.domain.group.dto.GroupResponseDto
import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.entity.GroupStatus
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.group.service.GroupService
import com.example.backend.domain.groupcategory.GroupCategory
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.voter.repository.VoterRepository
import com.example.backend.global.redis.service.RedisService
import com.example.backend.global.util.TestTokenProvider
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers
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
class GroupControllerTest {

    @Autowired
    private lateinit var groupService : GroupService
    @Autowired
    private lateinit var tokenProvider: TestTokenProvider
    @Autowired
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var groupRepository: GroupRepository
    @Autowired
    private lateinit var categoryRepository: CategoryRepository
    @Autowired
    private lateinit var voteRepository: VoteRepository
    @Autowired
    private lateinit var voterRepository: VoterRepository
    @MockitoBean
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var mvc : MockMvc
    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    @BeforeEach
    fun setUp() {

        memberRepository.deleteAll()
        groupRepository.deleteAll()
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate()
        em.createNativeQuery("ALTER TABLE \"groups\" ALTER COLUMN id RESTART WITH 1").executeUpdate()

        val member = Member(1L, "testUser", "test@test.com")
        memberRepository.save(member)
        val member2 = Member(2L, "testUser2", "test2@test.com")
        memberRepository.save(member2)

        accessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )
        refreshToken = tokenProvider.generateMemberRefreshToken()
        `when`(redisService.isValidRefreshToken(Mockito.anyString())).thenReturn(true)

        val category = Category("testCategory", CategoryType.STUDY)
        categoryRepository.save(category)
        val categories : MutableList<Category> = categoryRepository.findAll()

        for (i in 0 until 5){
            val group = Group("title$i","description$i",member,GroupStatus.RECRUITING,5)
            val groupCategories : MutableList<GroupCategory> = categories.map { GroupCategory(group,category) }.toMutableList()
            group.addGroupCategories(groupCategories)
            groupRepository.save(group)
            val groupId = group.id
            val vote1 = Vote(groupId = groupId!!, location = "장소1", address = "주소1", latitude = 11.11111, longitude = 11.11111)
            val vote2 = Vote(groupId = groupId!!, location = "장소2", address = "주소2", latitude = 11.11111, longitude = 11.11111)
            val vote3 = Vote(groupId = groupId!!, location = "장소3", address = "주소3", latitude = 11.11111, longitude = 11.11111)
            voteRepository.save(vote1)
            voteRepository.save(vote2)
            voteRepository.save(vote3)
            val voter = Voter(Voter.VoterId(member.id!!,groupId),member,vote1)
            val voter1 = Voter(Voter.VoterId(member2.id!!,groupId),member2,vote2)
            voterRepository.save(voter)
            voterRepository.save(voter1)
            if (0<i) group.status = GroupStatus.COMPLETED
        }
    }

    @Test
    @DisplayName("그룹 생성")
    fun t1() {
        val resultActions : ResultActions = mvc.perform(
                post("/groups")
                        .cookie(Cookie("accessToken",accessToken))
                        .cookie(Cookie("refreshToken",refreshToken))
                        .content("""
                                {
                                  "title": "제목1",
                                  "description": "내용1",
                                  "maxParticipants":5,
                                  "categoryIds": [1],
                                  "status":"RECRUITING"
                                }
                                """)
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        val groupResponseDto : GroupResponseDto = groupService.create(
            GroupRequestDto(
                "제목1",
                "내용1",
                5,
                Arrays.asList(1L),
                GroupStatus.RECRUITING),1L);
        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("createGroup"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(groupResponseDto.title))
                .andExpect(jsonPath("$.description").value(groupResponseDto.description))
                .andExpect(jsonPath("$.memberId").value(groupResponseDto.memberId))
                .andExpect(jsonPath("$.maxParticipants").value(groupResponseDto.maxParticipants))
                .andExpect(jsonPath("$.category").isArray())
//                .andExpect(jsonPath("$.category[0].id").value(1L))
                .andExpect(jsonPath("$.status").value(Matchers.equalTo("RECRUITING")))
    }

    @Test
    @DisplayName("그룹 전체 조회")
    fun t2() {
        val resultActions : ResultActions = mvc.perform(
                get("/groups")
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("listGroups"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
    }

    @Test
    @DisplayName("그룹 특정 조회")
    fun t3() {
        val resultActions : ResultActions = mvc.perform(
                get("/groups/{id}",1L)
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("getGroup"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.memberId").isNotEmpty())
                .andExpect(jsonPath("$.maxParticipants").isNotEmpty())
                .andExpect(jsonPath("$.category").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty())
    }

    @Test
    @DisplayName("그룹 수정")
    fun t4() {
        val resultActions : ResultActions = mvc.perform(
                put("/groups/{id}",1L)
                        .cookie( Cookie("accessToken",accessToken))
                        .cookie(Cookie("refreshToken",refreshToken))
                        .content("""
                                {
                                  "title": "제목2",
                                  "description": "내용3",
                                  "maxParticipants":6,
                                  "groupStatus":"RECRUITING"
                                }
                                """)
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("modifyGroup"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("제목2"))
                .andExpect(jsonPath("$.description").value("내용3"))
                .andExpect(jsonPath("$.maxParticipants").value(6))
                .andExpect(jsonPath("$.status").value("RECRUITING"))
    }

    @Test
    @DisplayName("그룹 삭제")
    fun t5() {
        val resultActions : ResultActions = mvc.perform(
                delete("/groups/{id}",1L)
                        .cookie( Cookie("accessToken",accessToken))
                        .cookie(Cookie("refreshToken",refreshToken))
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("deleteGroup"))
                .andExpect(status().isOk)
    }

    @Test
    @DisplayName("그룹 참가")
    fun t6() {
        val resultActions : ResultActions = mvc.perform(
            post("/groups/join")
                .cookie(Cookie("accessToken",accessToken))
                .cookie(Cookie("refreshToken",refreshToken))
                .content("""
                    {
                        "groupId": 1,
                        "memberId": 1
                    }
                """)
                .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
            .andExpect(handler().methodName("joinGroup"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("유저가 속한 그룹 조회")
    fun t7() {
        val resultActions : ResultActions = mvc.perform(
            get("/groups/member")
                .cookie( Cookie("accessToken",accessToken))
                .cookie(Cookie("refreshToken",refreshToken))
                .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
            .andExpect(handler().methodName("getGroupByMember"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
    }

    @Test
    @DisplayName("투표완료된 그룹명과 장소 조회")
    fun t8() {
        val resultActions : ResultActions = mvc.perform(
            get("/groups/location")
                .cookie( Cookie("accessToken",accessToken))
                .cookie(Cookie("refreshToken",refreshToken))
                .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
            .andExpect(handler().methodName("getLocationOfGroup"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
    }
}
