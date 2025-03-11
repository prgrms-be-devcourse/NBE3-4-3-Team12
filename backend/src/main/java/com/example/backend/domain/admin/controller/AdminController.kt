package com.example.backend.domain.admin.controller;

import com.example.backend.domain.admin.dto.AdminLoginRequest
import com.example.backend.domain.admin.service.AdminService
import com.example.backend.domain.group.service.GroupService
import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.global.redis.service.RedisService
import com.example.backend.global.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
	private val adminService: AdminService,
	private val groupService: GroupService,
	private val redisService: RedisService
) {

	// 관리자 로그인
	@PostMapping("/login")
	fun login(@RequestBody request: AdminLoginRequest, response: HttpServletResponse): ResponseEntity<String> {
		val admin = adminService.getAdmin(request.adminName, request.password)
		adminService.generateToken(admin, response)
		adminService.generateAndSaveRefreshToken(admin, response)
		return ResponseEntity.ok("로그인 성공")
		}

	// 관리자 로그아웃
	@PostMapping("/logout")
	fun logout(response: HttpServletResponse, request: HttpServletRequest): ResponseEntity<String> {
		adminService.logout(response, request)
		return ResponseEntity.ok("로그아웃 성공")
	}

	// 게시물 삭제
	@DeleteMapping("/group/{groupId}")
	fun deleteGroup(@PathVariable groupId: Long): ResponseEntity<Void> {
		groupService.deleteGroup(groupId)
		return ResponseEntity.noContent().build()
	}

	// 블랙리스트 처리
	@DeleteMapping("/members/{memberId}/blacklist")
	fun blacklistMember(@PathVariable memberId: String): ResponseEntity<Void> {
		redisService.blackListedMember(memberId)
		return ResponseEntity.noContent().build()
	}

	// 관리자 인증 확인
	@GetMapping
	fun isAdmin(): ResponseEntity<String> {
		val authentication: Authentication = SecurityContextHolder.getContext().authentication
		val principal: String = authentication.name
		return if (authentication.isAuthenticated && principal == "anonymousUser") {
			ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("인증되지 않은 관리자입니다.")
		} else {
			ResponseEntity.ok("관리자 인증되었습니다.")
		}
	}

	// 닉네임으로 유저 검색
	@GetMapping("/members/search")
	fun getMembersByNickName(@RequestParam nickname: String , response: HttpServletResponse): ResponseEntity<ApiResponse<List<MemberInfoDto>>> {
		val memberInfoDtoList = adminService.findMemberInfoDtosByNickname(nickname)
		return ResponseEntity.ok().body(ApiResponse(memberInfoDtoList))
	}
}