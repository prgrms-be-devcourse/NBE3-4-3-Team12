package com.example.backend.domain.voter.entity

import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*
import java.io.Serializable

@Entity
class Voter(
    @EmbeddedId
    val id: VoterId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("voteId")
    @JoinColumn(name = "vote_id", nullable = false)
    val vote: Vote
) : BaseEntity() {

    @Embeddable
    data class VoterId(
        val memberId: Long = 0L,
        val voteId: Long = 0L
    ) : Serializable
}
