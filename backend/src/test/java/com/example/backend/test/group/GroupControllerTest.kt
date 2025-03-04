package com.example.backend.test.group;

import com.example.backend.domain.group.controller.GroupController;
import com.example.backend.domain.group.dto.GroupRequestDto;
import com.example.backend.domain.group.dto.GroupResponseDto;
import com.example.backend.domain.group.entity.GroupStatus;
import com.example.backend.domain.group.service.GroupService;
import com.example.backend.domain.member.entity.Member;
import com.example.backend.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerTest {

    @Autowired
    private lateinit var groupService : GroupService

    @Autowired
    private lateinit var mvc : MockMvc

    @Autowired
    private lateinit var memberRepository : MemberRepository

    @Value("\${TEST_COOKIE}")
    private lateinit var cookie : String

    @Test
    @DisplayName("그룹 생성")
    fun t1() {
        val resultActions : ResultActions = mvc.perform(
                post("/groups")
                        .cookie(Cookie("accessToken",cookie))
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
        ).andDo(print());

        val groupResponseDto : GroupResponseDto = groupService.create(
            GroupRequestDto(
                "제목1",
                "내용1",
                5,
                Arrays.asList(1L),
                GroupStatus.RECRUITING),1L);
        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("createGroup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(groupResponseDto.title))
                .andExpect(jsonPath("$.description").value(groupResponseDto.description))
                .andExpect(jsonPath("$.memberId").value(groupResponseDto.memberId))
                .andExpect(jsonPath("$.maxParticipants").value(groupResponseDto.maxParticipants))
                .andExpect(jsonPath("$.category").isArray())
                .andExpect(jsonPath("$.category[0].id").value(1L))
                .andExpect(jsonPath("$.status").value(Matchers.equalTo("RECRUITING")));
    }

    @Test
    @DisplayName("그룹 전체 조회")
    fun t2() {
        val resultActions : ResultActions = mvc.perform(
                get("/groups")
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print());

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("listGroups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    @DisplayName("그룹 특정 조회")
    fun t3() {
        val resultActions : ResultActions = mvc.perform(
                get("/groups/{id}",1)
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print());

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("getGroup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.memberId").isNotEmpty())
                .andExpect(jsonPath("$.maxParticipants").isNotEmpty())
                .andExpect(jsonPath("$.category").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    @DisplayName("그룹 수정")
    fun t4() {
        val resultActions : ResultActions = mvc.perform(
                put("/groups/{id}",78L)
                        .cookie( Cookie("accessToken",cookie))
                        .content("""
                                {
                                  "title": "제목2",
                                  "description": "내용3",
                                  "maxParticipants":6,
                                  "groupStatus":"RECRUITING"
                                }
                                """)
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print());

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("modifyGroup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(78L))
                .andExpect(jsonPath("$.title").value("제목2"))
                .andExpect(jsonPath("$.description").value("내용3"))
                .andExpect(jsonPath("$.maxParticipants").value(6))
                .andExpect(jsonPath("$.status").value("RECRUITING"));
    }

    @Test
    @DisplayName("그룹 삭제")
    fun t5() {
        val resultActions : ResultActions = mvc.perform(
                delete("/groups/{id}",78L)
                        .cookie( Cookie("accessToken",cookie))
                        .contentType( MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print());

        resultActions.andExpect(handler().handlerType(GroupController::class.java))
                .andExpect(handler().methodName("deleteGroup"))
                .andExpect(status().isOk());
    }
}
