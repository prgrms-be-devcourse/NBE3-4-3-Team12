package com.example.backend.global.redis.service

import com.example.backend.global.redis.dao.RedisDao
import org.springframework.stereotype.Service

/**
 * RedisService
 * <p></p>
 * @author 100minha
 */
@Service
class RedisService(
    private val redisDao: RedisDao
) {

    fun save(key: String, value: String) {
        redisDao.save(key, value)
    }

    fun save(key: String, value: String, expirationTime: Long) {
        redisDao.save(key, value, expirationTime)
    }

    fun get(key: String): String? {
        return redisDao.get(key)
    }

    fun exists(key: String): Boolean {
        return redisDao.exists(key)
    }

    fun delete(key: String) {
        redisDao.delete(key)
    }

    fun setExpiration(key: String, expirationTimeInSeconds: Long) {
        redisDao.setExpiration(key, expirationTimeInSeconds)
    }

    fun addBlackList(refreshToken: String, expirationTimeInSeconds: Long) {
        redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds)
    }

    fun isValidRefreshToken(key: String): Boolean {
        return redisDao.exists(key) && redisDao.get(key) != "blacklisted"
    }

    fun blackListedMember(kakaoId: String) {
        val keys = redisDao.findAllKeys() // 모든 키 가져오기 (개선된 `scan` 사용)
        val values = redisDao.multiGet(keys)// 여러 값 한 번에 가져오기

        keys.zip(values).forEach { (key, value) ->
            if (value == "kakao: $kakaoId") {
                redisDao.save(key, "blacklisted") // 블랙리스트 처리
            }
        }
    }
}