package com.example.backend.domain.chat

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


/**
 * Redis를 이용한 채팅 메시지를 발행(Publisher)하는 클래스
 */

@Service
@Component
class ChatMessagePublisher(
    private val redisTemplate: RedisTemplate<String, ChatMessage>, // Redis에 메시지를 저장할 템플릿
    private val topic: ChannelTopic // 메시지를 발행할 채널 토픽
) {
    // 주어진 메시지를 Redis의 특정 채널(topic)로 발행하는 함수
    fun publish(message: ChatMessage) {
        redisTemplate.convertAndSend(topic.topic, message)
    }
}
