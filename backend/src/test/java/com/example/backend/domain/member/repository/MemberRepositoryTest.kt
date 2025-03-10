package com.example.backend.domain.member.repository

import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.domain.member.entity.Member
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles

/**
 * MemberRepositoryTest
 * <p></p>
 * @author 100minha
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
class MemberRepositoryTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var member: Member

    @BeforeEach
    fun setup() {
        memberRepository.deleteAll()
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate()

        member = memberRepository.save(
            Member(
                kakaoId = 1L,
                nickname = "testUser",
                email = "test@test.com"
            )
        )
    }

    @Test
    @DisplayName("save 성공 테스트")
    fun saveSuccessTest() {
        // given
        val member = Member(2L, "testUser2", "test2@test.com")

        // when
        val savedMember = memberRepository.save(member)

        // then
        assertThat(savedMember.id).isEqualTo(2L)
        assertThat(savedMember.kakaoId).isEqualTo(2L)
        assertThat(savedMember.nickname).isEqualTo("testUser2")
        assertThat(savedMember.email).isEqualTo("test2@test.com")
    }

    @Test
    @DisplayName("save 실패 테스트 - 이미 존재하는 kakaoId로 저장 시도")
    fun saveFailTest_WhenKakaoIdAlreadyExists() {
        // given
        val member = Member(1L, "testUser2", "test2@test.com")

        // when
        // then
        assertThrows<DataIntegrityViolationException> { // 데이터 무결성 위반 예외 발생
            memberRepository.save(member)
        }
    }

    @Test
    @DisplayName("findById 성공 테스트")
    fun findByIdSuccessTest() {
        // given
        val optionalMember = memberRepository.findById(1L)

        // when
        assertThat(optionalMember.isPresent).isTrue
        val member = optionalMember.get()

        // then
        assertThat(member.id).isEqualTo(1L)
        assertThat(member.kakaoId).isEqualTo(1L)
        assertThat(member.nickname).isEqualTo("testUser")
        assertThat(member.email).isEqualTo("test@test.com")
    }

    @Test
    @DisplayName("findById 실패 테스트 - 존재하지 않는 id로 조회 시도")
    fun findByIdFailTest_WhenMemberNotExists() {
        // given
        val optionalMember = memberRepository.findById(2L)

        // when
        // then
        assertThat(optionalMember.isPresent).isFalse
    }

    @Test
    @DisplayName("existsByKakaoId 성공 테스트")
    fun existsByKakaoIdSuccessTest() {
        // given
        // when
        val exists = memberRepository.existsByKakaoId(1L)

        // then
        assertThat(exists).isTrue
    }

    @Test
    @DisplayName("existsByKakaoId 실패 테스트 - 존재하지 않는 kakaoId로 조회 시도")
    fun existsByKakaoIdFailTest_WhenMemberNotExists() {
        // given
        // when
        val exists = memberRepository.existsByKakaoId(2L)

        // then
        assertThat(exists).isFalse
    }

    @Test
    @DisplayName("findByKakaoId 성공 테스트")
    fun findByKakaoIdSuccessTest() {
        // given
        val optionalMember = memberRepository.findByKakaoId(1L)

        // when
        assertThat(optionalMember.isPresent).isTrue
        val member = optionalMember.get()

        // then
        assertThat(member.id).isEqualTo(1L)
        assertThat(member.kakaoId).isEqualTo(1L)
        assertThat(member.nickname).isEqualTo("testUser")
        assertThat(member.email).isEqualTo("test@test.com")
    }

    @Test
    @DisplayName("findByKakaoId 실패 테스트 - 존재하지 않는 kakaoId로 조회 시도")
    fun findByKakaoIdFailTest_WhenMemberNotExists() {
        // given
        val optionalMember = memberRepository.findByKakaoId(2L)

        // when
        // then
        assertThat(optionalMember.isPresent).isFalse
    }

    @Test
    @DisplayName("findMemberInfoDtoById 성공 테스트")
    fun findMemberInfoDtoByIdSuccessTest() {
        // given
        val memberInfoDtoOfMember = MemberInfoDto.of(member)
        val optionalMemberInfoDto = memberRepository.findMemberInfoDtoById(1L)

        // when
        assertThat(optionalMemberInfoDto.isPresent).isTrue
        val memberInfoDto = optionalMemberInfoDto.get()

        // then
        assertThat(memberInfoDto).isEqualTo(memberInfoDtoOfMember)
    }

    @Test
    @DisplayName("findMemberInfoDtoById 실패 테스트 - 존재하지 않는 id로 조회 시도")
    fun findMemberInfoDtoByIdFailTest_WhenMemberNotExists() {
        // given
        val optionalMemberInfoDto = memberRepository.findMemberInfoDtoById(2L)

        // when
        // then
        assertThat(optionalMemberInfoDto.isPresent).isFalse
    }
}