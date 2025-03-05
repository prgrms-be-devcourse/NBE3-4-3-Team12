package com.example.backend.global.auth.jwt;

import com.example.backend.domain.admin.service.AdminGetService
import com.example.backend.global.auth.exception.AuthErrorCode
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.auth.util.JwtUtil
import com.example.backend.global.auth.util.TokenProvider
import com.example.backend.global.response.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class AdminAuthFilter(
	private val jwtUtil: JwtUtil,
	private val tokenProvider: TokenProvider,
	private val adminGetService: AdminGetService,
	private val cookieService: CookieService,
	private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

	@Throws(ServletException::class, IOException::class)
	override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
		// 요청 헤더에서 JWT 토큰 가져오기
		val accessToken = cookieService.getAccessTokenFromCookie(request)
		val refreshToken = cookieService.getRefreshTokenFromCookie(request)

		// 토큰이 유효하면 SecurityContext에 인증 정보 저장
		if (accessToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
			handleException(AuthErrorCode.TOKEN_EXPIRED, request, response)
			return
		}

		try {
			val tokenStatus = jwtUtil.validateToken(accessToken)

			when (tokenStatus) {
				TokenStatus.VALID -> {
					val authentication = jwtUtil.getAuthentication(accessToken)
					SecurityContextHolder.getContext().authentication = authentication
					chain.doFilter(request, response)
				}
				TokenStatus.EXPIRED -> {
					if (jwtUtil.isRefreshTokenValid(refreshToken)) {
						val admin = adminGetService.getAdminByRefreshToken(refreshToken)
						val newAccessToken = tokenProvider.generateToken(admin)
						cookieService.addAccessTokenToCookie(newAccessToken, response)
						val newAuthentication = jwtUtil.getAuthentication(newAccessToken)
						SecurityContextHolder.getContext().authentication = newAuthentication
						chain.doFilter(request, response)
					} else {
						handleException(AuthErrorCode.TOKEN_REISSUE_FAILED, request, response)
					}
				}
				TokenStatus.MALFORMED -> handleException(AuthErrorCode.AUTHORIZATION_FAILED, request, response)
				TokenStatus.INVALID -> handleException(AuthErrorCode.INVALID_TOKEN, request, response)
			}
		} catch (e: Exception) {
			handleException(AuthErrorCode.AUTHORIZATION_FAILED, request, response)
		}
	}

	override fun shouldNotFilter(request: HttpServletRequest): Boolean {
		val path = request.requestURI
		val method = request.method
		return !((path.startsWith("/admin") && method in setOf("GET", "DELETE")) ||
				(path.startsWith("/categories") && method in setOf("POST", "PUT", "DELETE")))
	}

	@Throws(IOException::class)
	private fun handleException(ex: AuthErrorCode, request: HttpServletRequest, response: HttpServletResponse) {
		cookieService.clearTokenFromCookie(response)
		response.status = ex.httpStatus.value()
		response.contentType = "application/json;charset=UTF-8"

		val errorResponse = ErrorResponse.of(
			ex.message,
			ex.code,
			request.requestURI
		)
		objectMapper.writeValue(response.writer, errorResponse)
	}
}
