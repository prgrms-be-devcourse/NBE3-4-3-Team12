package com.example.backend.domain.group.service

import lombok.extern.slf4j.Slf4j
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Slf4j
@Service
class GroupViewService(private val redisTemplate: StringRedisTemplate) {

    private val viewKeyPrefix = "group:views:"
    private val userViewedPrefix = "group:user:viewed:"

    fun incrementViewCount(groupId: Long, userId: Long) {
        val groupKey = "$viewKeyPrefix$groupId"
        val userViewKey = "$userViewedPrefix$groupId:$userId"

        log.info("groupkey는 : $groupKey")
        log.info("userKey는 : $userViewKey")
        if (redisTemplate.opsForValue().get(userViewKey) == null) {
            redisTemplate.opsForValue().increment(groupKey)
            redisTemplate.opsForValue().set(userViewKey, "viewed", 24, TimeUnit.HOURS) // 24시간 동안만 조회한 것으로 처리
        }
    }

    fun getViewCount(groupId: Long): Long {
        val key = "$viewKeyPrefix$groupId"
        return redisTemplate.opsForValue().get(key)?.toLong() ?: 0
    }
}
