package com.example.backend.domain.admin.service;

import com.example.backend.domain.admin.entity.Admin
import com.example.backend.domain.admin.exception.AdminErrorCode
import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.admin.repository.AdminRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminGetService(
    private val adminRepository: AdminRepository
) {

    // 관리자 name 으로 가져오기
    @Transactional(readOnly = true)
    fun getAdminByName(name: String): Admin {
        return adminRepository.findByAdminName(name)
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)
    }

    // 리프레시 토큰으로 가져오기
    @Transactional(readOnly = true)
    fun getAdminByRefreshToken(refreshToken: String): Admin {
        return adminRepository.findByRefreshToken(refreshToken)
            ?: throw AdminException(AdminErrorCode.NOT_FOUND_ADMIN)
    }
}
