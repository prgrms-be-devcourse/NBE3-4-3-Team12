package com.example.backend.global.auth.service

import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.domain.member.service.MemberService
import com.example.backend.global.auth.model.CustomUserDetails
import com.example.backend.global.auth.util.JwtUtil
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * CustomUserDetailService
 * @author 100minha
 */
@Service
class CustomUserDetailService(
    private val memberService: MemberService,
    private val jwtUtil: JwtUtil
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): CustomUserDetails {
        val member = memberService.findById(username.toLong())
        return CustomUserDetails(MemberInfoDto(member))
    }

    fun loadUserByAccessToken(accessToken: String): CustomUserDetails {
        return CustomUserDetails(jwtUtil.getMemberInfoDto(accessToken))
    }
}
