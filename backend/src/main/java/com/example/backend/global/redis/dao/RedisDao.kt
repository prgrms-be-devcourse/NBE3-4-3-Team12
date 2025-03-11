package com.example.backend.global.redis.dao

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit


/**
 * RedisDao
 * <p></p>
 * @author 100minha
 */
@Repository
class RedisDao(
    private val redisTemplate: RedisTemplate<String, String>
) {

    // 데이터 저장 (만료 시간 설정 없이)
    fun save(key: String, value: String) {
        redisTemplate.opsForValue().set(key, value)
    }

    // 데이터 저장 (만료 시간 설정 포함)
    fun save(key: String, value: String, expirationTime: Long) {
        redisTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.SECONDS)
    }

    // 데이터 조회
    fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    // 데이터 존재 여부 확인
    fun exists(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    // 데이터 삭제
    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    // 만료 시간 갱신 (시간 단위는 초)
    fun setExpiration(key: String, expirationTimeInSeconds: Long) {
        redisTemplate.expire(key, expirationTimeInSeconds, TimeUnit.SECONDS)
    }
}