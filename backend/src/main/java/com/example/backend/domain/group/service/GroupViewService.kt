package com.example.backend.domain.group.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class GroupViewService(private val redisTemplate: StringRedisTemplate) {

    private val viewKeyPrefix = "group:views:"

    fun incrementViewCount(groupId: Long) {
        val key = "$viewKeyPrefix$groupId"
        redisTemplate.opsForValue().increment(key)
    }

    fun getViewCount(groupId: Long): Long {
        val key = "$viewKeyPrefix$groupId"
        return redisTemplate.opsForValue().get(key)?.toLong() ?: 0
    }
}
