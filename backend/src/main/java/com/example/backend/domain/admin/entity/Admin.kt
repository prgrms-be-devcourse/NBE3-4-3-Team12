package com.example.backend.domain.admin.entity;

import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Admin (
    @Column(unique = true)
    val adminName: String,
    val password: String,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    val role: Role = Role.ADMIN

    @Column
    var refreshToken: String? = null

    @Column
    var refreshTokenExpiryDate: LocalDateTime? = null

    fun setRefreshToken(refreshToken: String?, expiryDate: LocalDateTime?) {
        this.refreshToken = refreshToken
        this.refreshTokenExpiryDate = expiryDate
    }
}

enum  class Role {
    ADMIN
}