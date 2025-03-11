package com.example.backend.global.redis.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class RedisServiceTest {

    @Autowired
    private lateinit var redisService: RedisService

    private val refreshToken = "468cb628-5f71-4e34-89fb-7e5b6ce7575b"

    @BeforeEach
    fun init() {
        // 실제 Redis 서버에 토큰 저장
        redisService.save(refreshToken, "admin", 5L)
    }

    @Test
    @DisplayName("리프레시 토큰 저장 테스트")
    fun refreshTokenShouldBeStored() {
        val storedValue = redisService.get(refreshToken)
        assertThat(storedValue).isEqualTo("admin")
    }

    @Test
    @DisplayName("리프레시 토큰 블랙리스트 테스트")
    fun refreshTokenShouldBeDeleted() {
        redisService.addBlackList(refreshToken, 5L)
        val storedValue = redisService.get(refreshToken)
        assertThat(storedValue).isEqualTo("blacklisted")
    }

    @Test
    @DisplayName("리프레시 토큰 검증 성공 테스트")
    fun refreshTokenValidationShouldSucceed() {
        val exists = redisService.exists(refreshToken)
        assertThat(exists).isTrue()
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 테스트")
    fun refreshTokenValidationShouldFail() {
        val nonExistentToken = "invalid-token-123"
        val exists = redisService.exists(nonExistentToken)
        assertThat(exists).isFalse()
    }

    @Test
    @DisplayName("리프레시 토큰 만료 테스트")
    fun refreshTokenExpirationTimeShouldBeSetCorrectly() {
        // 짧은 만료 시간 설정 (100ms)
        val shortLivedToken = "short-lived-token"

        redisService.save(shortLivedToken, "admin", 1L)

        // 토큰이 아직 유효한지 확인
        val storedValue = redisService.get(shortLivedToken)
        assertThat(storedValue).isEqualTo("admin")

        // 만료될 때까지 대기
        Thread.sleep(1000)

        // 만료 후 확인
        val expiredValue = redisService.get(shortLivedToken)
        assertThat(expiredValue).isNull()
    }

    @AfterEach
    fun cleanUp() {
        // 테스트 후 토큰 삭제
        redisService.delete(refreshToken)
    }
}