package com.example.backend.global.auth.service

import com.example.backend.global.auth.util.CookieUtil
import com.example.backend.global.auth.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * CookieService
 * 쿠키 관련 로직을 처리하는 클래스
 * @author 100mi
 */
@Service
class CookieService(
    private val cookieUtil: CookieUtil,
    private val jwtUtil: JwtUtil
) {

    fun addAccessTokenToCookie(accessToken: String, response: HttpServletResponse) {
        cookieUtil.addTokenToCookie(
            "accessToken", accessToken,
            TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getAccessTokenExpirationTime()),
            response
        )
    }

    fun addRefreshTokenToCookie(refreshToken: String, response: HttpServletResponse) {
        cookieUtil.addTokenToCookie(
            "refreshToken", refreshToken,
            TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getRefreshTokenExpirationTime()),
            response
        )
    }

    fun addRefreshTokenToCookieWithSameSiteNone(refreshToken: String, response: HttpServletResponse) {
        cookieUtil.addTokenToCookieWithSameSiteNone(
            "refreshToken", refreshToken,
            TimeUnit.MILLISECONDS.toSeconds(jwtUtil.getRefreshTokenExpirationTime()), response
        )
    }

    fun getAccessTokenFromCookie(request: HttpServletRequest): String? {
        return cookieUtil.getTokenFromCookie("accessToken", request)
    }

    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return cookieUtil.getTokenFromCookie("refreshToken", request)
    }

    fun clearTokenFromCookie(response: HttpServletResponse) {
        cookieUtil.addTokenToCookie("accessToken", "", 0, response)
        cookieUtil.addTokenToCookie("refreshToken", "", 0, response)
    }
}
