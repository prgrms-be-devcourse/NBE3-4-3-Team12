package com.example.backend.domain.vote.controller

import com.example.backend.domain.vote.dto.VoteRequestDto
import com.example.backend.domain.vote.dto.VoteResponseDto
import com.example.backend.domain.vote.dto.VoteResultDto
import com.example.backend.domain.vote.service.VoteService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/votes")
class VoteController(
    private val voteService: VoteService  // 생성자 주입, non-null 타입
) {
    // 생성
    @PostMapping("/groups/{groupId}/votes")
    fun createVote(
        @PathVariable groupId: Long,
        @RequestBody request: VoteRequestDto
    ): ResponseEntity<VoteResponseDto> {
        return ResponseEntity.ok(voteService.createVote(groupId, request))
    }

    //투표 목록조회
    @GetMapping("/groups/{groupId}/votes")
    fun getVotesByGroupId(
        @PathVariable groupId: Long
    ): ResponseEntity<List<VoteResponseDto>> {
        return ResponseEntity.ok(voteService.findAllByGroupId(groupId))
    }

    //단일 투표 상세조회
    @GetMapping("/groups/{groupId}/votes/{voteId}")
    fun getVote(
        @PathVariable groupId: Long,
        @PathVariable voteId: Long
    ): ResponseEntity<VoteResponseDto> {
        return ResponseEntity.ok(voteService.findById(groupId, voteId))
    }

    //투표 수정
    @PutMapping("/groups/{groupId}/votes/{voteId}")
    fun modifyVote(
        @PathVariable groupId: Long,
        @PathVariable voteId: Long,
        @Valid @RequestBody requestDto: VoteRequestDto
    ): ResponseEntity<VoteResponseDto> {
        return ResponseEntity.ok(voteService.modifyVote(groupId, voteId, requestDto))
    }

    @DeleteMapping("/groups/{groupId}/votes/{voteId}")
    fun deleteVote(
        @PathVariable groupId: Long,
        @PathVariable voteId: Long
    ): ResponseEntity<Void> {
        voteService.deleteVote(groupId, voteId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/groups/{groupId}/most-voted")
    fun getMostVotedLocations(
        @PathVariable groupId: Long
    ): ResponseEntity<VoteResultDto> {
        return ResponseEntity.ok(voteService.getMostVotedLocations(groupId))
    }
}