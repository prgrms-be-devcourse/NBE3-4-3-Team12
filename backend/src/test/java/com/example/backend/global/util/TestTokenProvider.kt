package com.example.backend.global.util

import com.example.backend.global.auth.util.JwtUtil
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

/**
 * TestTokenProvider
 *
 *
 * @author 100mi
 */
@Component
@Profile("test")
class TestTokenProvider(
    private val jwtUtil: JwtUtil
) {
    fun generateMemberAccessToken(id: Long, nickname: String, email: String): String {
        return Jwts.builder()
            .setSubject(nickname) // 사용자 nickname
            .claim("id", id) // Member primary-key
            .claim("email", email)
            .setIssuedAt(Date()) // 토큰 발급 시간
            .setExpiration(Date(System.currentTimeMillis() + 10000)) // 테스트용으로만 사용될 토큰이므로 보안상 만료 시간 10초로 설정
            .signWith(jwtUtil.key, SignatureAlgorithm.HS256) // 서명 알고리즘 및 키
            .compact()
    }
}
