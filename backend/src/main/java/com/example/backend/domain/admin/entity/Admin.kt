package com.example.backend.domain.admin.entity;

import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*

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

}

enum  class Role {
    ADMIN
}