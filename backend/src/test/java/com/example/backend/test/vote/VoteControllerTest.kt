package com.example.backend.test.vote

import com.example.backend.domain.vote.controller.VoteController
import com.example.backend.domain.vote.dto.MostVotedLocationDto
import com.example.backend.domain.vote.dto.VoteRequestDto
import com.example.backend.domain.vote.dto.VoteResponseDto
import com.example.backend.domain.vote.dto.VoteResultDto
import com.example.backend.domain.vote.service.VoteService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [VoteController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [WebSecurityConfiguration::class]
        )
    ],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
class VoteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var voteService: VoteService

    private val groupId = 1L
    private val voteId = 1L
    private lateinit var voteRequest: VoteRequestDto
    private lateinit var voteResponse: VoteResponseDto

    @BeforeEach
    fun setup() {
        // 테스트에 사용할 데이터 준비
        voteRequest = VoteRequestDto(
            location = "서울시 강남구",
            address = "테헤란로 123",
            latitude = 37.5665,
            longitude = 126.9780
        )

        voteResponse = VoteResponseDto(
            id = voteId,
            location = "서울시 강남구",
            address = "테헤란로 123",
            latitude = 37.5665,
            longitude = 126.9780,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    @DisplayName("투표 생성 테스트")
    fun createVoteTest() {
        // Given
        `when`(voteService.createVote(eq(groupId), any())).thenReturn(voteResponse)

        // When
        val result: MvcResult = mockMvc.perform(
            post("/votes/groups/$groupId/votes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voteRequest))
        ).andReturn()

        // Then
        assertEquals(200, result.response.status)

        val responseContent = result.response.contentAsString
        val responseDto = objectMapper.readValue(responseContent, VoteResponseDto::class.java)

        assertEquals(voteId, responseDto.id)
        assertEquals("서울시 강남구", responseDto.location)
        assertEquals("테헤란로 123", responseDto.address)
        assertEquals(37.5665, responseDto.latitude)
        assertEquals(126.9780, responseDto.longitude)
    }



}