package com.example.backend.domain.member.dto

import com.example.backend.domain.member.entity.Member

/**
 * MemberTokenReissueDto
 * 토큰 갱신할 때 사용할 리프레시토큰까지 갖는 Dto
 * @author 100minha
 */
data class MemberTokenReissueDto(
    val id: Long,
    val nickname: String,
    val email: String,
    val refreshToken: String
) {
    companion object {
        fun of(member: Member, reIssuedRefreshToken: String): MemberTokenReissueDto {
            return MemberTokenReissueDto(
                member.id!!,
                member.nickname,
                member.email,
                reIssuedRefreshToken
            )
        }
    }
}
