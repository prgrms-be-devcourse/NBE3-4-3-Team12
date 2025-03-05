
package com.example.backend.global.auth.util;

import com.example.backend.domain.admin.service.AdminGetService
import com.example.backend.domain.member.dto.MemberInfoDto
import com.example.backend.global.auth.exception.AuthErrorCode
import com.example.backend.global.auth.exception.AuthException
import com.example.backend.global.auth.jwt.TokenStatus
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime

/**
 * JwtUtil
 * jwt 관련 유틸 클래스
 * @author 100minha
 */
@Component
class JwtUtil(
	private val adminGetService: AdminGetService,
	@Value("\${spring.security.jwt.secret-key}")
	private val secretKey: String,
	@Value("\${spring.security.jwt.access-token.expiration}")
	private val accessTokenExpirationTime: Long,
	@Value("\${spring.security.jwt.refresh-token.expiration}")
	private val refreshTokenExpirationTime: Long
) {
	val key: Key = Keys.hmacShaKeyFor(secretKey.toByteArray())

	fun getAccessTokenExpirationTime(): Long = accessTokenExpirationTime
	fun getRefreshTokenExpirationTime(): Long = refreshTokenExpirationTime

	fun validateToken(token: String): TokenStatus {
		return try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
			TokenStatus.VALID
		} catch (e: ExpiredJwtException) {
			TokenStatus.EXPIRED
		} catch (e: MalformedJwtException) {
			TokenStatus.MALFORMED
		} catch (e: IllegalArgumentException) {
			TokenStatus.INVALID
		}
	}

	fun getAuthentication(token: String): Authentication {
		val claims = parseToken(token)
		val adminName = claims.subject
		val authorities = listOf(getAuthoritiesFromClaims(claims))

		return UsernamePasswordAuthenticationToken(adminName, "", authorities)
	}

	private fun getAuthoritiesFromClaims(claims: Claims): GrantedAuthority {
		val role = claims["role", String::class.java]
		return SimpleGrantedAuthority(role)
	}

	fun parseToken(token: String): Claims {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
	}

	fun getAdminRole(token: String): String {
		return parseToken(token)["role", String::class.java]
	}

	fun getMemberInfoDto(token: String): MemberInfoDto {
		val claims = parseToken(token)
		return MemberInfoDto(
			claims["id", Integer::class.java].toLong(),
			claims.subject,
			claims["email", String::class.java]
		)
	}

	fun isRefreshTokenValid(refreshToken: String): Boolean {
		val admin = adminGetService.getAdminByRefreshToken(refreshToken)
		return admin.refreshTokenExpiryDate?.isAfter(LocalDateTime.now())
			?: throw AuthException(AuthErrorCode.TOKEN_EXPIRED)
	}
}