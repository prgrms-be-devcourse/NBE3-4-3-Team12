package com.example.backend.domain.group.service

import com.example.backend.global.redis.service.RedisService
import org.springframework.stereotype.Service

@Service
class GroupViewService(
    private val redisService: RedisService) {
    fun incrementViewCount(groupId: Long, userId: Long) {
        if (!redisService.isUserViewed(groupId, userId)) {
            redisService.markUserAsViewed(groupId, userId)
        }
    }
    fun getViewCount(groupId: Long): Long {
        return redisService.getViewCount(groupId)
    }
}