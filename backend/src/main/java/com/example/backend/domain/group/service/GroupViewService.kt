package com.example.backend.domain.group.service

import com.example.backend.global.redis.service.RedisService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GroupViewService(private val redisService: RedisService) {

    private val log = LoggerFactory.getLogger(GroupViewService::class.java)

    private val viewKeyPrefix = "group:views:"
    private val userViewedPrefix = "group:user:viewed:"

    fun incrementViewCount(groupId: Long, userId: Long) {
        val groupKey = "$viewKeyPrefix$groupId"
        val userViewKey = "$userViewedPrefix$groupId:$userId"

        log.info("groupkey는 : $groupKey")
        log.info("userKey는 : $userViewKey")
        if (!redisService.isUserViewed(groupId, userId)) {
            val newViewCount = redisService.incrementViewCount(groupId)
            redisService.markUserAsViewed(groupId, userId)

            log.info("New view count for group $groupId: $newViewCount")
        }
    }

    fun getViewCount(groupId: Long): Long {
        val groupKey = "$viewKeyPrefix$groupId"
        return redisService.getViewCount(groupId)
    }
}
