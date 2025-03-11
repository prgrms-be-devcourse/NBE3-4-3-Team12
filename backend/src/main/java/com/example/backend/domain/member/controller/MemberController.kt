package com.example.backend.domain.member.controller

import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.domain.member.dto.MemberModifyRequestDto
import com.example.backend.domain.member.service.MemberService
import com.example.backend.global.auth.model.CustomUserDetails
import com.example.backend.global.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/members")
@RestController
class MemberController(
    private val memberService: MemberService
) {

    @GetMapping
    fun getCurrentMember(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<MemberInfoDto>> {

        val memberDto = MemberInfoDto(
            customUserDetails.userId,
            customUserDetails.username,
            customUserDetails.email
        )
        return ResponseEntity.ok().body(ApiResponse.of(memberDto))
    }

    @PutMapping
    fun modify(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody @Valid memberModifyDto: MemberModifyRequestDto,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<MemberInfoDto>> {

        val memberInfoDto = memberService.modify(customUserDetails.userId, memberModifyDto, response)
        return ResponseEntity.ok().body(ApiResponse.of(memberInfoDto))
    }

    @GetMapping("/search")
    fun getMembersByNickName(@RequestParam nickname: String , response: HttpServletResponse): ResponseEntity<ApiResponse<List<MemberInfoDto>>> {
        val memberInfoDtoList = memberService.findMemberInfoDtosByNickname(nickname)
        return ResponseEntity.ok().body(ApiResponse.of(memberInfoDtoList))
    }
}
