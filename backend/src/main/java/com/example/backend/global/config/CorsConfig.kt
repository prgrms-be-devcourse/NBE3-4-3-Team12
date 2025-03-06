package com.example.backend.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * CorsConfig
 * Cors설정 클래스
 * @author 100minha
 */
@Configuration
class CorsConfig(
    @Value("\${CLIENT_BASE_URL}")
    private val clientBaseUrl: String)
{
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()

        val corsConfig = CorsConfiguration()
        corsConfig.allowCredentials = true
        corsConfig.addAllowedOrigin(clientBaseUrl)
        corsConfig.addAllowedHeader("*")
        corsConfig.addExposedHeader("Authorization")
        corsConfig.addAllowedMethod("*")

        source.registerCorsConfiguration("/**", corsConfig)
        return CorsFilter(source)
    }
}
