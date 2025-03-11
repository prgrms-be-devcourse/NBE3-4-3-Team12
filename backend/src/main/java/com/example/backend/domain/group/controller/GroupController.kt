package com.example.backend.domain.group.controller;

import com.example.backend.domain.group.dto.*
import com.example.backend.domain.group.service.GroupService
import com.example.backend.domain.group.service.GroupTopViewService
import com.example.backend.domain.group.service.GroupViewService
import com.example.backend.global.auth.model.CustomUserDetails
import jakarta.validation.Valid;
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
class GroupController(
    private val groupService: GroupService,
    private val groupViewService: GroupViewService,
    private val groupTopViewService: GroupTopViewService
) {
    @PostMapping
    fun createGroup (
        @RequestBody @Valid requestDto : GroupRequestDto,
        @AuthenticationPrincipal customUserDetails : CustomUserDetails
    ) : ResponseEntity<GroupResponseDto> {
        log.info("New group creation requested")
        val memberId : Long = customUserDetails.userId
        val response : GroupResponseDto = groupService.create(requestDto, memberId)
        return ResponseEntity(response, HttpStatusCode.valueOf(200))
    }

    @GetMapping
    fun listGroups() : ResponseEntity<List<GroupResponseDto>>{
        log.info("Listing all groups are requested");
        val response : List<GroupResponseDto> = groupService.findNotDeletedAllGroups()
        return ResponseEntity(response, HttpStatusCode.valueOf(200))
    }

    @GetMapping("/{groupId}")
    fun getGroup(@PathVariable("groupId") id : Long, @AuthenticationPrincipal customUserDetails : CustomUserDetails) : ResponseEntity<GroupResponseDto>{
        log.info("Getting group by groupId {}$id");
        val memberId : Long = customUserDetails.userId
        groupViewService.incrementViewCount(id,memberId);
        val response : GroupResponseDto = groupService.findGroup(id)
        return  ResponseEntity(response, HttpStatusCode.valueOf(200))
    }

    @GetMapping("/topViews")
    fun getTopPosts(): ResponseEntity<List<GroupResponseDto>> {
        val response = groupTopViewService.getTop3ViewedGroups()
        return ResponseEntity(response, HttpStatusCode.valueOf(200))
    }

    @DeleteMapping("/{groupId}")
    fun deleteGroup(@PathVariable("groupId") id : Long) : ResponseEntity<GroupResponseDto>{
        log.info("Deleting a particular group is being requested")
        groupService.deleteGroup(id)
        return ResponseEntity(null,HttpStatus.OK)
    }

    @PutMapping("/{groupId}")
    fun modifyGroup(
        @PathVariable("groupId") groupId : Long,
        @RequestBody @Valid modifyRequestDto : GroupModifyRequestDto
    ) : ResponseEntity<GroupResponseDto>{
        log.info("Modifying a particular group is being requested")
        val response : GroupResponseDto = groupService.modifyGroup(groupId,modifyRequestDto)
        return ResponseEntity(response,HttpStatus.valueOf(200))
    }

    @PostMapping("/join")
    fun joinGroup(
        @RequestBody @Valid joinGroupRequestDto : JoinGroupRequestDto,
        @AuthenticationPrincipal userDetails : CustomUserDetails
    ) : ResponseEntity<String>{
        val groupId : Long = joinGroupRequestDto.groupId
        val memberId : Long = userDetails.userId

        if (memberId == null || groupId == null) {
            return ResponseEntity.badRequest().body("그룹 또는 회원의 데이터가 없습니다.")
        }

        groupService.joinGroup(groupId, memberId)

        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/member")
    fun getGroupByMember(
        @AuthenticationPrincipal userDetails : CustomUserDetails
    ) : ResponseEntity<List<GroupResponseDto>>{
        val response : List<GroupResponseDto> =  groupService.getGroupsByMemberId(userDetails.userId)
        return ResponseEntity(response, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/location")
    fun getLocationOfGroup(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ) : ResponseEntity<List<GroupLocationDto>>{
        val memberId : Long = userDetails.userId
        val response : List<GroupLocationDto> = groupService.getLocationOfCompletedGroup(memberId)
        return ResponseEntity(response, HttpStatusCode.valueOf(200))
    }
}