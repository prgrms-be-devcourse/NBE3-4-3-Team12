package com.example.backend.global.config

import com.example.backend.domain.chat.ChatMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.nio.charset.StandardCharsets


/**
 * RedisConfig
 * Redis 설정
 * @author 100minha
 */
@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory("localhost", 6379)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer(StandardCharsets.UTF_8)
        template.valueSerializer = StringRedisSerializer(StandardCharsets.UTF_8)
        return template
    }

    @Bean
    fun chatMessageRedisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, ChatMessage> {
        val template = RedisTemplate<String, ChatMessage>()
        template.connectionFactory = factory
        template.setDefaultSerializer(Jackson2JsonRedisSerializer(ChatMessage::class.java))
        return template
    }
}