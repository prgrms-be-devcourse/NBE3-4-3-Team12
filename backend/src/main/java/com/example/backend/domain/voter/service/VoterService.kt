package com.example.backend.domain.voter.service

import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.dto.MemberVoteResponseDto
import com.example.backend.domain.voter.dto.VoterDTO
import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.voter.exception.VoterErrorCode
import com.example.backend.domain.voter.exception.VoterException
import com.example.backend.domain.voter.repository.VoterRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class VoterService(
	private val voterRepository: VoterRepository,
	private val voteRepository: VoteRepository,
	private val memberRepository: MemberRepository,
	private val groupRepository: GroupRepository,
	private val groupMemberRepository: GroupMemberRepository
) {

	fun getVotersByVoteId(voteId: Long): List<VoterDTO> {
		return voterRepository.findByIdVoteId(voteId)
			.map { VoterDTO.from(it) }
	}

	@Transactional
	fun addVoter(groupId: Long, voteId: Long, memberId: Long): Voter {
		val vote = voteRepository.findByIdAndGroupId(voteId, groupId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_VOTE) }

		val member = memberRepository.findById(memberId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_MEMBER) }

		val group = groupRepository.findById(groupId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_GROUP) }

		if (!groupMemberRepository.existsByGroupAndMember(group, member)) {
			throw VoterException(VoterErrorCode.NOT_GROUP_MEMBER)
		}

		val voterId = Voter.VoterId(memberId, voteId)
		if (voterRepository.existsById(voterId)) {
			throw VoterException(VoterErrorCode.ALREADY_VOTED)
		}

		return voterRepository.save(Voter(voterId, member, vote))
	}

	@Transactional
	fun removeVoter(groupId: Long, voteId: Long, memberId: Long) {
		val vote = voteRepository.findByIdAndGroupId(voteId, groupId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_VOTE) }

		val member = memberRepository.findById(memberId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_MEMBER) }

		val group = groupRepository.findById(groupId)
			.orElseThrow { VoterException(VoterErrorCode.NOT_FOUND_GROUP) }

		if (!groupMemberRepository.existsByGroupAndMember(group, member)) {
			throw VoterException(VoterErrorCode.NOT_GROUP_MEMBER)
		}

		val voterId = Voter.VoterId(memberId, voteId)
		if (!voterRepository.existsById(voterId)) {
			throw VoterException(VoterErrorCode.NOT_VOTED)
		}

		voterRepository.deleteById(voterId)
	}

	fun getVoteIdsByGroupIdAndMemberId(groupId: Long, memberId: Long): MemberVoteResponseDto {
		val voteIds = voteRepository.findAllByGroupId(groupId).map { it.id }
		val voteResultIds = voterRepository.findVoteIdsByVoteIdsAndMemberId(voteIds, memberId)

		return MemberVoteResponseDto(voteResultIds)
	}
}
