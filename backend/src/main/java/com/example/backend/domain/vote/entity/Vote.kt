package com.example.backend.domain.vote.entity

import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*

//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
@Entity
class Vote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "group_id")
    val groupId: Long,

    val location: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
) : BaseEntity()