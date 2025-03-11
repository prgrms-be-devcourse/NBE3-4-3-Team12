package com.example.backend.domain.group.service

import com.example.backend.global.redis.service.RedisService
import org.springframework.stereotype.Service

@Service
class GroupViewService(
    private val redisService: RedisService) {
    fun incrementViewCount(groupId: Long, userId: Long) {
        if (!redisService.isUserViewed(groupId, userId)) {
            // 사용자를 조회한 것으로 표시
            redisService.markUserAsViewed(groupId, userId)

            // 조회수 증가
            redisService.incrementViewCount(groupId)
        }
    }
    fun getViewCount(groupId: Long): Long {

        return redisService.getViewCount(groupId)
    }
}