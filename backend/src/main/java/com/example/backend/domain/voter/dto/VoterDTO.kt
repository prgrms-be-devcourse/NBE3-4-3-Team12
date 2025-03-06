package com.example.backend.domain.voter.dto

import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.member.entity.Member

data class VoterDTO(
    val memberId: Long,
    val voteId: Long
) {
    companion object {
        fun from(voter: Voter): VoterDTO {
            return VoterDTO(
                memberId = voter.id.memberId,
                voteId = voter.id.voteId
            )
        }
    }

    fun toEntity(member: Member, vote: Vote): Voter {
        return Voter(
            id = Voter.VoterId(memberId, voteId),
            member = member,
            vote = vote
        )
    }
}
