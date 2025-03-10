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
}