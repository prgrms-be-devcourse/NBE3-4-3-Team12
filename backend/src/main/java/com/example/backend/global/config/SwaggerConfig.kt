package com.example.backend.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * SwaggerConfig
 * 스웨거 api 문서화 관련 설정
 * @author 100minha
 */
@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("2차 프로젝트 12팀 API").version("v1")) // 보안 요구사항 설정 (Bearer 토큰 대신 쿠키 사용)
            .addSecurityItem(SecurityRequirement().addList("cookieAuth"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "cookieAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.COOKIE) // 요청 시 쿠키를 자동으로 포함
                            .name("accessToken")
                    )
            ) // 쿠키 이름
    }

    @Bean
    fun integrationApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("api")
            .pathsToMatch("/**")
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("adminApi")
            .pathsToMatch("/admin/**")
            .build()
    }

    @Bean
    fun categoryApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("categoryApi")
            .pathsToMatch("/categories/**")
            .build()
    }

    @Bean
    fun groupApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("groupApi")
            .pathsToMatch("/groups/**")
            .build()
    }

    @Bean
    fun memberApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("memberApi")
            .pathsToMatch("/members/**")
            .build()
    }

    @Bean
    fun voteApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("voteApi")
            .pathsToMatch("/votes/**")
            .build()
    }

    @Bean
    fun voterApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("voterApi")
            .pathsToMatch("/voters/**")
            .build()
    }

    @Bean
    fun authApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("authApi")
            .pathsToMatch("/auth/**")
            .build()
    }

    @Bean
    fun chatApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("chatApi")
            .pathsToMatch("/chat/**")
            .build()
    }
}
