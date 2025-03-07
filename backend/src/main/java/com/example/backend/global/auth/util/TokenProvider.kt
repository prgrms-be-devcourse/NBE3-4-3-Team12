package com.example.backend.global.auth.util;

import com.example.backend.domain.admin.entity.Admin
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class TokenProvider(
	private val jwtUtil: JwtUtil
) {

	// JWT 토큰 생성
	fun generateToken(admin: Admin): String {
		return Jwts.builder()
			.setSubject(admin.id.toString())  // 사용자 이름 (관리자 ID)
			.claim("role", admin.role)  // 역할 (권한 정보)
			.setIssuedAt(Date())  // 토큰 발급 시간
			.setExpiration(Date(System.currentTimeMillis() + jwtUtil.getAccessTokenExpirationTime()))  // 만료시간
			.signWith(jwtUtil.key)  // 서명 알고리즘 및 키
			.compact()
	}

	fun generateMemberAccessToken(id: Long, nickname: String, email: String): String {
		return Jwts.builder()
			.setSubject(nickname)  // 사용자 nickname
			.claim("id", id)    // Member primary-key
			.claim("email", email)
			.setIssuedAt(Date())  // 토큰 발급 시간
			.setExpiration(Date(System.currentTimeMillis() + jwtUtil.getAccessTokenExpirationTime()))  // 만료시간
			.signWith(jwtUtil.key, SignatureAlgorithm.HS256)  // 서명 알고리즘 및 키
			.compact()
	}

	// 리프레시 토큰 생성
	fun generateRefreshToken(): String {
		return UUID.randomUUID().toString()
	}

	// 리프레시 토큰 만료 시간 계산
	fun getRefreshTokenExpiryDate(): Long {
		return TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getRefreshTokenExpirationTime())
	}
}
