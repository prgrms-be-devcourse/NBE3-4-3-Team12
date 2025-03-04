package com.example.backend.domain.member.dto

import com.example.backend.domain.member.entity.Member

/**
 * MemberInfoDto
 * 사용자 기본 정보만 담는 dto
 * @author 100minha
 */
data class MemberInfoDto(
    val id: Long,
    val nickname: String,
    val email: String
) {
    companion object {
        fun of(member: Member): MemberInfoDto {
            return MemberInfoDto(
                id = member.id!!,
                nickname = member.nickname,
                email = member.email
            )
        }
    }
}
