package com.example.backend.domain.voter.repository

import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.voter.entity.Voter.VoterId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface VoterRepository : JpaRepository<Voter, VoterId> {

    // 특정 사용자가 이미 투표에 참여했는지 확인
    override fun existsById(voterId: VoterId): Boolean

    // 특정 투표(voteId)에 참여한 Voter 목록 조회
    fun findByIdVoteId(voteId: Long): List<Voter>

    // 특정 사용자의 투표 기록 삭제 (투표 취소)
    override fun deleteById(voterId: VoterId)

    // 특정 투표에 참여한 인원 수 조회
    @Query("SELECT COUNT(v) FROM Voter v WHERE v.id.voteId = :voteId")
    fun countVoters(@Param("voteId") voteId: Long): Long

    // 주어진 투표 ID 목록 중 현재 사용자가 투표한 투표의 ID만 반환
    @Query("SELECT v.vote.id FROM Voter v WHERE v.vote.id IN :voteIds AND v.member.id = :memberId")
    fun findVoteIdsByVoteIdsAndMemberId(@Param("voteIds") voteIds: List<Long>, @Param("memberId") memberId: Long): List<Long>

    // 특정 투표 ID 목록에 해당하는 투표 삭제
    fun deleteByVoteIdIn(voteIds: List<Long>)
}
