package com.example.backend.domain.voter.controller

import com.example.backend.domain.voter.dto.MemberVoteResponseDto
import com.example.backend.domain.voter.dto.VoterDTO
import com.example.backend.domain.voter.service.VoterService
import com.example.backend.global.auth.model.CustomUserDetails
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/voters")
class VoterController(
	private val voterService: VoterService
) {
	private val logger = LoggerFactory.getLogger(VoterController::class.java)

	@PostMapping("/{groupId}/{voteId}")
	fun addVoter(
		@PathVariable groupId: Long,
		@PathVariable voteId: Long,
		@AuthenticationPrincipal customUserDetails: CustomUserDetails
	): ResponseEntity<VoterDTO> {
		logger.info("New voter participation requested: groupId={}, voteId={}, memberId={}", groupId, voteId, customUserDetails.userId)
		val response = VoterDTO.from(voterService.addVoter(groupId, voteId, customUserDetails.userId))
		return ResponseEntity.ok(response)
	}

	@GetMapping("/{voteId}")
	fun getVotersByVote(@PathVariable voteId: Long): ResponseEntity<List<VoterDTO>> {
		logger.info("Getting voter list for voteId={}", voteId)
		val response = voterService.getVotersByVoteId(voteId)
		return ResponseEntity.ok(response)
	}

	@DeleteMapping("/{groupId}/{voteId}")
	fun removeVoter(
		@PathVariable groupId: Long,
		@PathVariable voteId: Long,
		@AuthenticationPrincipal customUserDetails: CustomUserDetails
	): ResponseEntity<Void> {
		logger.info("Voter removal requested: groupId={}, voteId={}, memberId={}", groupId, voteId, customUserDetails.userId)
		voterService.removeVoter(groupId, voteId, customUserDetails.userId)
		return ResponseEntity.ok().build()
	}

	@GetMapping("/group/{groupId}")
	fun memberVoteStatus(
		@PathVariable groupId: Long,
		@AuthenticationPrincipal customUserDetails: CustomUserDetails
	): ResponseEntity<MemberVoteResponseDto> {
		logger.info("Getting vote id list By groupId={}", groupId)
		val memberId = customUserDetails.userId
		val memberVoteResponseDto = voterService.getVoteIdsByGroupIdAndMemberId(groupId, memberId)
		return ResponseEntity.ok(memberVoteResponseDto)
	}
}
