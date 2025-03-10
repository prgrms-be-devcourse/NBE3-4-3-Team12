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

    // 조회수 증가
    fun incrementViewCount(groupId: Long): Long {
        val groupKey = "group:views:$groupId"
        val currentViewCount = getViewCount(groupId)
        val newViewCount = currentViewCount + 1
        redisDao.save(groupKey, newViewCount.toString())
        return newViewCount
    }

    // 조회수 가져오기
    fun getViewCount(groupId: Long): Long {
        val groupKey = "group:views:$groupId"
        return redisDao.get(groupKey)?.toLong() ?: 0
    }

    // 사용자 조회 여부 체크
    fun isUserViewed(groupId: Long, userId: Long): Boolean {
        val userViewKey = "group:user:viewed:$groupId:$userId"
        return redisDao.exists(userViewKey)
    }

    // 조회한 사용자 마킹
    fun markUserAsViewed(groupId: Long, userId: Long) {
        val userViewKey = "group:user:viewed:$groupId:$userId"
        redisDao.save(userViewKey, "viewed", 24 * 60 * 60L) // 24시간 동안만 조회한 것으로 처리
    }

    fun addBlackList(refreshToken: String, expirationTimeInSeconds: Long) {
        redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds)
    }

    fun isValidRefreshToken(key: String): Boolean {
        return redisDao.exists(key) && redisDao.get(key) != "blacklisted"
    }
}