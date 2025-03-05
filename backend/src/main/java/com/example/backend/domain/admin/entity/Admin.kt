package com.example.backend.domain.admin.entity;

import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Admin (
    @Column(unique = true)
    val adminName: String,
    val password: String,

    @Enumerated(EnumType.STRING)
    val role: Role,

    @Column
    var refreshToken: String? = null,

    @Column
    var refreshTokenExpiryDate: LocalDateTime? = null
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    constructor(adminName: String, password: String) : this(
        adminName, password, Role.ADMIN)

    fun setRefreshToken(refreshToken: String?, expiryDate: LocalDateTime?) {
        this.refreshToken = refreshToken
        this.refreshTokenExpiryDate = expiryDate
    }
}

enum  class Role {
    ADMIN
}