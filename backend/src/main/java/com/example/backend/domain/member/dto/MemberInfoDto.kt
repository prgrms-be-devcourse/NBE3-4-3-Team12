package com.example.backend.domain.member.dto

import com.example.backend.domain.member.entity.Member
import com.example.backend.global.auth.model.CustomUserDetails

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

    constructor(member: Member) : this(
        id = member.id!!,
        nickname = member.nickname,
        email = member.email
    )

    constructor(customUserDetails: CustomUserDetails) : this(
        id = customUserDetails.userId,
        nickname = customUserDetails.username,
        email = customUserDetails.email
    )
}
