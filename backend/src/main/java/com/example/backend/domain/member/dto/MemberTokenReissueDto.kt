package com.example.backend.domain.member.dto

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

    constructor(memberInfoDto: MemberInfoDto, reIssuedRefreshToken: String) : this(
        memberInfoDto.id,
        memberInfoDto.nickname,
        memberInfoDto.email,
        reIssuedRefreshToken
    )
}
