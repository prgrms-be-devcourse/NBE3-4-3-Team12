package com.example.backend.domain.member.entity

import com.example.backend.domain.member.dto.MemberModifyRequestDto
import com.example.backend.global.auth.kakao.dto.KakaoUserInfoResponseDto
import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*

@Entity
class Member(
    @Column(unique = true)
    val kakaoId: Long,
    var nickname: String,
    var email: String
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    constructor(kakaoUserInfoDto: KakaoUserInfoResponseDto) : this(
        kakaoId = kakaoUserInfoDto.id,
        nickname = kakaoUserInfoDto.properties.nickname,
        email = kakaoUserInfoDto.kakaoAccount.email
    )

    fun modify(memberModifyDto: MemberModifyRequestDto) {
        this.nickname = memberModifyDto.nickname
    }
}
