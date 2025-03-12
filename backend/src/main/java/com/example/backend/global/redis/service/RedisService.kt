package com.example.backend.global.redis.service

import com.example.backend.domain.group.dto.GroupResponseDto
import com.example.backend.global.redis.dao.RedisDao
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

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

    fun getKeys(pattern: String): Set<String> {
        return redisDao.findKeysByPattern(pattern)
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

    // 조회수 가져오기
    fun getViewCount(groupId: Long): Long {
        val groupKey = "group:views:$groupId"
        return redisDao.get(groupKey)?.toLong() ?: 0
    }

    // 사용자 조회 여부 체크
    fun isUserViewed(groupId: Long, userId: Long): Boolean {
        val userViewKey = "group:user:viewed:$groupId:$userId"
        val viewed = redisDao.exists(userViewKey)
        println("Checking if user $userId viewed group $groupId: $viewed")
        return redisDao.exists(userViewKey)
    }

    // 조회한 사용자 마킹
    fun markUserAsViewed(groupId: Long, userId: Long) {
        val userViewKey = "group:user:viewed:$groupId:$userId"
        //redisDao.save(userViewKey, "viewed", TimeUnit.SECONDS.toSeconds(30));
        redisDao.save(userViewKey, "viewed", TimeUnit.DAYS.toSeconds(1)) // 24시간 동안만 조회한 것으로 처리
    }

    fun getAllKeys(): List<String> {
        return redisDao.getAllKeys()
    }
    // 그룹 정보 저장 (예시: GroupResponseDto를 JSON 문자열로 변환하여 저장)
    fun saveGroupInfo(groupId: Long, groupResponseDto: GroupResponseDto) {
        val objectMapper = jacksonObjectMapper()
        val groupJson = objectMapper.writeValueAsString(groupResponseDto)
        redisDao.save("group:top3:$groupId", groupJson)
    }

    // 그룹 정보 가져오기
    fun getGroupInfo(groupId: Long): GroupResponseDto? {
        val groupJson = redisDao.get("group:top3:$groupId")
        return if (groupJson != null) {
            val objectMapper = jacksonObjectMapper()
            objectMapper.readValue(groupJson, GroupResponseDto::class.java)
        } else {
            null
        }
    }
    fun incrementViewCount(groupId: Long) {
        val groupKey = "group:views:$groupId"
        val currentCount = get(groupKey)?.toLong() ?: 0
        save(groupKey, (currentCount + 1).toString())
    }

    fun addBlackList(refreshToken: String, expirationTimeInSeconds: Long) {
        redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds)
    }

    fun isValidRefreshToken(key: String): Boolean {
        return redisDao.get(key) != "blacklisted"
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
