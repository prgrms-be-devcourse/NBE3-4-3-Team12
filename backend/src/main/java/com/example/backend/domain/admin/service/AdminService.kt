package com.example.backend.domain.admin.service;

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.exception.AdminErrorCode
import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.admin.repository.AdminRepository
import com.example.backend.global.auth.service.CookieService
import com.example.backend.global.auth.util.JwtUtil
import com.example.backend.global.auth.util.TokenProvider
import com.example.backend.global.redis.service.RedisService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cookieService: CookieService,
    private val tokenProvider: TokenProvider,
    private val redisService: RedisService,
    private val jwtUtil: JwtUtil
) {

    // 로그인 검증
    @Transactional(readOnly = true)
    fun getAdmin(adminName: String, password: String): Admin {
        val admin: Admin = adminRepository.findByAdminName(adminName)
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)

        if (!passwordEncoder.matches(password, admin.password)) {
            throw AdminException(AdminErrorCode.INVALID_CREDENTIALS)
        }

        return admin
    }

    // 엑세스 토큰 쿠키 저장
    @Transactional
    fun generateToken(admin: Admin, response: HttpServletResponse) {
        val accessToken: String = tokenProvider.generateToken(admin)
        cookieService.addAccessTokenToCookie(accessToken, response)
    }

    // 리프레시 토큰 저장
    @Transactional
    fun generateAndSaveRefreshToken(admin: Admin, response: HttpServletResponse) {
        val refreshToken = tokenProvider.generateRefreshToken()
        val expiryDate = tokenProvider.getRefreshTokenExpiryDate()

        redisService.save(refreshToken, admin.adminName, expiryDate)
        cookieService.addRefreshTokenToCookie(refreshToken, response)
    }

    // admin 객체에 리프레시 토큰 만료
    @Transactional
    fun logout(response: HttpServletResponse, request: HttpServletRequest) {
        cookieService.clearTokenFromCookie(response)

        val refreshToken = cookieService.getRefreshTokenFromCookie(request)
        if (refreshToken != null) {
            redisService.addBlackList(refreshToken, jwtUtil.getRefreshTokenExpirationTime())
        } else  {
            throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)
        }
    }
}