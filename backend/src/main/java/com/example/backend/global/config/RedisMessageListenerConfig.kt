package com.example.backend.global.config

import com.example.backend.domain.chat.ChatMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RedisMessageListenerConfig {

    @Bean
    fun redisContainer(
        connectionFactory: RedisConnectionFactory,
        subscriber: ChatMessageSubscriber
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(MessageListenerAdapter(subscriber), ChannelTopic("chat"))
        return container
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("chat")
    }
}
