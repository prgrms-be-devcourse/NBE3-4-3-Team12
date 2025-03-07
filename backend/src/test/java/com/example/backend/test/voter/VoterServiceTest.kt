package com.example.backend.test.voter

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.entity.Voter
import com.example.backend.domain.voter.exception.VoterErrorCode
import com.example.backend.domain.voter.exception.VoterException
import com.example.backend.domain.voter.repository.VoterRepository
import com.example.backend.domain.voter.service.VoterService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.util.*

@ExtendWith(MockitoExtension::class)
class VoterServiceTest {
	@InjectMocks
	private lateinit var voterService: VoterService

	@Mock
	private lateinit var voterRepository: VoterRepository

	@Mock
	private lateinit var voteRepository: VoteRepository

	@Mock
	private lateinit var memberRepository: MemberRepository

	@Mock
	private lateinit var groupRepository: GroupRepository

	@Mock
	private lateinit var groupMemberRepository: GroupMemberRepository

	private val groupId = 1L
	private val voteId = 1L
	private val memberId = 1L

	private lateinit var vote: Vote
	private lateinit var member: Member
	private lateinit var group: Group
	private lateinit var voterId: Voter.VoterId
	private lateinit var voter: Voter

	@BeforeEach
	fun setUp() {

		// Mock 동작 설정: save() 호출 시 실제 객체 반환
		`when`(memberRepository.save(any())).thenAnswer { it.arguments[0] as Member }
		`when`(voteRepository.save(any())).thenAnswer { it.arguments[0] as Vote }
		`when`(groupRepository.save(any())).thenAnswer { it.arguments[0] as Group }

		// Mock 데이터를 생성하여 저장
		member = memberRepository.save(Member(kakaoId = 1L, nickname = "", email = ""))
		vote = voteRepository.save(Vote(groupId = 1L, location = "", address = "", latitude = 0.0, longitude = 0.0))
		group = groupRepository.save(Group(title = "", description = "", member = member, maxParticipants = 10))

		// JPA @GeneratedValue 필드 강제 설정
		ReflectionTestUtils.setField(member, "id", memberId)
		ReflectionTestUtils.setField(vote, "id", voteId)
		ReflectionTestUtils.setField(group, "id", groupId)

		// Voter 객체 생성
		voterId = Voter.VoterId(memberId, voteId)
		voter = Voter(voterId, member, vote)
	}

	@Test
	@DisplayName("투표 참여 테스트")
	fun addVoter() {
		// Mock 객체의 동작 설정
		`when`(voteRepository.findByIdAndGroupId(voteId, groupId)).thenReturn(Optional.of(vote))
		`when`(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
		`when`(groupRepository.findById(groupId)).thenReturn(Optional.of(group))
		`when`(groupMemberRepository.existsByGroupAndMember(group, member)).thenReturn(true)
		`when`(voterRepository.existsById(voterId)).thenReturn(false)
		`when`(voterRepository.save(any(Voter::class.java))).thenReturn(voter)

		// 테스트 실행
		val result = voterService.addVoter(groupId, voteId, memberId)

		assertThat(result).isNotNull()
		assertThat(result.id).isEqualTo(voterId)
		verify(voterRepository, times(1)).save(any(Voter::class.java))
	}

	@Test
	@DisplayName("중복 투표시 예외 발생")
	fun addVoter_AlreadyVoted() {
		`when`(voteRepository.findByIdAndGroupId(voteId, groupId)).thenReturn(Optional.of(vote))
		`when`(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
		`when`(groupRepository.findById(groupId)).thenReturn(Optional.of(group))
		`when`(groupMemberRepository.existsByGroupAndMember(group, member)).thenReturn(true)
		`when`(voterRepository.existsById(voterId)).thenReturn(true)

		assertThatThrownBy { voterService.addVoter(groupId, voteId, memberId) }
			.isInstanceOf(VoterException::class.java)
			.hasMessageContaining(VoterErrorCode.ALREADY_VOTED.message)
	}

	@Test
	@DisplayName("투표에 참여한 멤버 목록 조회 테스트")
	fun getVotersByVote() {
		`when`(voterRepository.findByIdVoteId(voteId)).thenReturn(Collections.singletonList(voter))

		val result = voterService.getVotersByVoteId(voteId)

		assertThat(result).hasSize(1)
		assertThat(result[0].memberId).isEqualTo(voter.id.memberId)
		assertThat(result[0].voteId).isEqualTo(voter.id.voteId)
	}

	@Test
	@DisplayName("투표 취소 테스트")
	fun removeVoter() {
		`when`(voteRepository.findByIdAndGroupId(voteId, groupId)).thenReturn(Optional.of(vote))
		`when`(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
		`when`(groupRepository.findById(groupId)).thenReturn(Optional.of(group))
		`when`(groupMemberRepository.existsByGroupAndMember(group, member)).thenReturn(true)
		`when`(voterRepository.existsById(voterId)).thenReturn(true)
		doNothing().`when`(voterRepository).deleteById(voterId)

		voterService.removeVoter(groupId, voteId, memberId)
		verify(voterRepository, times(1)).deleteById(voterId)
	}

	@Test
	@DisplayName("참여하지 않은 투표를 취소할 경우 예외 발생")
	fun removeVoter_NotVoted() {
		`when`(voteRepository.findByIdAndGroupId(voteId, groupId)).thenReturn(Optional.of(vote))
		`when`(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
		`when`(groupRepository.findById(groupId)).thenReturn(Optional.of(group))
		`when`(groupMemberRepository.existsByGroupAndMember(group, member)).thenReturn(true)
		`when`(voterRepository.existsById(voterId)).thenReturn(false)

		assertThatThrownBy { voterService.removeVoter(groupId, voteId, memberId) }
			.isInstanceOf(VoterException::class.java)
			.hasMessageContaining(VoterErrorCode.NOT_VOTED.message)
	}
}