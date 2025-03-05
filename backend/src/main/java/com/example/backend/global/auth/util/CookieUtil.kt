package com.example.backend.global.auth.util

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

/**
 * CookieUtil
 * 쿠키에 관련된 로직을 수행할 유틸 클래스
 * @author 100minha
 */
@Component
class CookieUtil {
    /**
     * 쿠키에 name의 토큰을 저장하는 메서드
     * @param name
     * @param value
     * @param expiration
     * @param response
     */
    fun addTokenToCookie(name: String, value: String, expiration: Long, response: HttpServletResponse) {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(expiration)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    /**
     * 쿠키에서 name의 토큰을 받아오는 메서드
     * @param name
     * @param request
     * @return
     */
    fun getTokenFromCookie(name: String, request: HttpServletRequest): String? {
        return request.cookies
            ?.firstOrNull { it.name == name }
            ?.value
    }
}
