package com.example.backend.domain.admin.repository;

import com.example.backend.domain.admin.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminRepository : JpaRepository<Admin, Long> {

    fun findByAdminName(adminName: String): Admin?

    fun findByRefreshToken(refreshToken: String): Admin?
}
