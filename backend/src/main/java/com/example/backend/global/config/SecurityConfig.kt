package com.example.backend.global.config;

import com.example.backend.global.auth.jwt.MemberAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * SecurityConfig
 * 시큐리티 관련 설정 클래스
 * @author 100minha
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
	private val memberAuthFilter: MemberAuthFilter,
	private val adminAuthFilter: MemberAuthFilter,
	private val corsConfig: CorsConfig
) {
	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf { it.ignoringRequestMatchers("/h2-console/**").disable() }  // H2 콘솔 사용을 위해 CSRF 비활성화
			.headers { it.frameOptions { frame -> frame.disable() } }  // H2 콘솔 화면을 위해 필요
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.httpBasic { it.disable() }
			.formLogin { it.disable() }
			.logout { it.disable() }
			.addFilter(corsConfig.corsFilter())
			.authorizeHttpRequests {
				it.requestMatchers("/h2-console/**").permitAll()  // H2 콘솔 접근 허용
					.anyRequest().permitAll()
			}
			.addFilterBefore(memberAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
			.addFilterBefore(adminAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

		return http.build()
	}

	@Bean
	fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
		authenticationConfiguration.authenticationManager

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
