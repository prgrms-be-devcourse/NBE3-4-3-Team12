package com.example.backend.test.voter

import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.voter.repository.VoterRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest


@DataJpaTest
class VoterRepositoryTest @Autowired constructor(
    private val voterRepository: VoterRepository,
    private val voteRepository: VoteRepository,
    private val memberRepository: MemberRepository
) {

    private lateinit var vote: Vote
    private lateinit var member: Member
    private lateinit var voterId: Voter.VoterId
    private lateinit var voter: Voter

    @BeforeEach
    fun setUp() {
        member = memberRepository.save(Member(kakaoId = 1L, nickname = "", email = ""))
        vote = voteRepository.save(Vote(groupId = 1L, location = "", address = "", latitude = 0.0, longitude = 0.0))

//        ReflectionTestUtils.setField(vote, "id", 1L)


        voterId = Voter.VoterId(member.id!!, vote.id!!)
        voter = Voter(id = voterId, member = member, vote = vote)
    }

    @Test
    @DisplayName("투표 참여 저장 테스트")
    fun saveVoterTest() {
        // when
        val savedVoter = voterRepository.save(voter)

        // then
        assertThat(savedVoter).isNotNull
        assertThat(savedVoter.id).isEqualTo(voterId)
    }

    @Test
    @DisplayName("투표에 참여한 멤버 목록 조회 테스트")
    fun findVotersByVoteTest() {
        // given
        voterRepository.save(voter)

        // when
        val voters = voterRepository.findByIdVoteId(vote.id!!)

        // then
        assertThat(voters).isNotEmpty()
        assertThat(voters[0].id).isEqualTo(voterId)
    }

    @Test
    @DisplayName("멤버가 특정 투표에 참여했는지 확인")
    fun existsByIdTest() {
        // given
        voterRepository.save(voter)

        // when
        val exists = voterRepository.existsById(voterId)

        // then
        assertThat(exists).isTrue
    }

    @Test
    @DisplayName("투표 취소 테스트")
    fun deleteVoterTest() {
        // given
        voterRepository.save(voter)

        // when
        voterRepository.deleteById(voterId)
        val deletedVoter = voterRepository.findById(voterId)

        // then
        assertThat(deletedVoter).isEmpty
    }
}
