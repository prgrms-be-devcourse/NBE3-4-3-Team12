package com.example.backend.domain.group.service

import com.example.backend.domain.group.dto.GroupResponseDto
import com.example.backend.global.redis.service.RedisService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
@Service
class GroupTopViewService(
    private val redisService: RedisService,
    private val groupService: GroupService
) {

    private val viewKeyPrefix = "group:views:"
    private val topGroupKey = "group:top3"

    // 상위 3개의 인기 게시글을 조회
    fun getTop3ViewedGroups(): List<GroupResponseDto> {
        val keys = redisService.getAllKeys()
        val views = keys.mapNotNull { key ->
            val groupId = key.removePrefix(viewKeyPrefix).toLongOrNull()
            val viewCount = groupId?.let { redisService.getViewCount(it) }
            groupId?.let { it to viewCount }
        }
        // 조회수를 기준으로 내림차순 정렬 후 상위 3개 선택
        val topGroupIds = views.sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        return topGroupIds.map { groupId ->
            var groupResponseDto = redisService.getGroupInfo(groupId)
            // Redis에 그룹 정보가 없다면 DB에서 조회 후 Redis에 저장
            if (groupResponseDto == null) {
                val groupResponse = groupService.findGroup(groupId)
                groupResponseDto = groupResponse
                redisService.saveGroupInfo(groupId, groupResponseDto)
            }
            groupResponseDto
        }
    }


    @Scheduled(cron = "0 0 0 * * SUN")
    fun showTop3Posts() {
        redisService.delete(topGroupKey)
        val topPosts = getTop3ViewedGroups()
        topPosts.forEachIndexed { index, group ->
            redisService.saveGroupInfo(group.id, group)
        }
    }
}
