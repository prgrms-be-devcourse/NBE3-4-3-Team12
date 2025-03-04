package com.example.backend.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * WebClientConfig
 * WebClient 스프링 빈 등록 클래스
 * @author 100minha
 */
@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }
}