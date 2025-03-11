package com.example.backend.domain.member.service

import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.domain.member.dto.MemberModifyRequestDto
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.exception.MemberErrorCode
import com.example.backend.domain.member.exception.MemberException
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.global.auth.kakao.dto.KakaoUserInfoResponseDto
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.auth.util.TokenProvider
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val tokenProvider: TokenProvider,
    private val cookieService: CookieService
) {

    @Transactional(readOnly = true)
    fun findById(id: Long): Member {
        return memberRepository.findById(id).orElseThrow {
            MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        }
    }

    @Transactional(readOnly = true)
    fun findByKakaoId(kakaoId: Long): Member {
        return memberRepository.findByKakaoId(kakaoId).orElseThrow {
            MemberException(
                MemberErrorCode.MEMBER_NOT_FOUND
            )
        }
    }

    @Transactional(readOnly = true)
    fun existsByKakaoId(kakaoId: Long): Boolean {
        return memberRepository.existsByKakaoId(kakaoId)
    }

    @Transactional(readOnly = true)
    fun findMemberInfoDtoById(id: Long): MemberInfoDto {
        return memberRepository.findMemberInfoDtoById(id).orElseThrow {
            MemberException(
                MemberErrorCode.MEMBER_NOT_FOUND
            )
        }
    }

    @Transactional
    fun join(kakaoUserInfoDto: KakaoUserInfoResponseDto) {
        memberRepository.save(Member.of(kakaoUserInfoDto))
    }

    @Transactional
    fun modify(
        id: Long, memberModifyDto: MemberModifyRequestDto,
        response: HttpServletResponse
    ): MemberInfoDto {
        val member = findById(id)
        member.modify(memberModifyDto)
        // 사용자 정보 수정 후 수정된 정보로 액세스 토큰 재발급
        val reissuedAccessToken = tokenProvider.generateMemberAccessToken(
            member.id!!, member.nickname, member.email
        )

        cookieService.addAccessTokenToCookie(reissuedAccessToken, response)
        return MemberInfoDto.of(member)
    }

    fun findMemberInfoDtosByNickname(nickname: String): List<MemberInfoDto> {
        val memberList = memberRepository.findMemberInfoDtosByNickname(nickname)

        return memberList.takeIf { it.isNotEmpty() }
            ?: throw MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
    }
}
