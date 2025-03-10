package com.example.backend.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * CacheConfig
 * <p></p>
 * @author 100minha
 */
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val objectMapper = ObjectMapper()  // Jackson의 ObjectMapper 인스턴스를 생성 (JSON 직렬화/역직렬화에 사용)
            .registerModules(JavaTimeModule())  // Java 8 날짜/시간(`java.time.LocalDateTime` 등)을 처리할 수 있도록 `JavaTimeModule` 등록
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)  // 날짜/시간을 타임스탬프(숫자)로 변환하지 않고 문자열(예: "2025-03-10T16:44:21")로 변환하도록 설정
            .apply {
                activateDefaultTyping(
                    polymorphicTypeValidator, // 직렬화/역직렬화 시 다형성(polymorphism) 처리를 위한 유효성 검사기 (보안 목적)
                    ObjectMapper.DefaultTyping.NON_FINAL // final이 아닌 클래스들에 대해 기본 타입 정보를 포함하여 직렬화하도록 설정
                )
            }

        val genericJackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        val cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory)
        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer)
            ) // Value Serializer 변경
            .entryTtl(Duration.ofMinutes(10L))

        return RedisCacheManager.builder(cacheWriter)
            .cacheDefaults(cacheConfig)
            .build()
    }
}