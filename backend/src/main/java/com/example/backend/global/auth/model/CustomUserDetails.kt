package com.example.backend.global.auth.model

import com.example.backend.domain.member.dto.MemberInfoDto
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * CustomUserDetails
 * @author 100minha
 */
class CustomUserDetails(
    private val memberInfoDto: MemberInfoDto
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return listOf(SimpleGrantedAuthority("USER"))
    }

    override fun getPassword(): String? {
        return null
    }

    override fun getUsername(): String {
        return memberInfoDto.nickname
    }

    val userId: Long
        get() = memberInfoDto.id

    val email: String
        get() = memberInfoDto.email

}
