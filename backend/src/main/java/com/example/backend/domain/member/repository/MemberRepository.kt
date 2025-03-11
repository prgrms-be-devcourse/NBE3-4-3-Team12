package com.example.backend.domain.member.repository

import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByKakaoId(kakaoId: Long): Boolean

    fun findByKakaoId(kakaoId: Long): Optional<Member>

    @Query(
        "SELECT new com.example.backend.domain.member.dto.MemberInfoDto(m.id, m.nickname, m.email) " +
                "FROM Member m WHERE m.id = :id"
    )
    fun findMemberInfoDtoById(id: Long): Optional<MemberInfoDto>

    @Query(
        "SELECT new com.example.backend.domain.member.dto.MemberInfoDto(m.id, m.nickname, m.email) " +
                "FROM Member m WHERE m.nickname = :nickname"
    )
    fun findMemberInfoDtosByNickname(nickname: String): List<MemberInfoDto>
}
