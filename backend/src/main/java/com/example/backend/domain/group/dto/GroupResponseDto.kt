package com.example.backend.domain.group.dto;

import com.example.backend.domain.category.dto.CategoryResponseDto
import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.entity.GroupStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class GroupResponseDto(
    val id : Long,
    val title : String,
    val description : String,
    val memberId : Long,
    val author : String,
    val maxParticipants : Int,
    val status : GroupStatus,
    val category : List<CategoryResponseDto>,

    val createdAt: String,
    val modifiedAt: String,
    val viewCount : Long
) {
    constructor(group: Group) : this(
        id = group.id,
        title = group.title,
        description = group.description,
        memberId = group.member.id!!,
        author = group.member.nickname,
        maxParticipants = group.maxParticipants,
        status = group.status,
        category = group.groupCategories.map { CategoryResponseDto(it.category) },
        createdAt = group.createdAt?.toString() ?: "",
        modifiedAt = group.modifiedAt?.toString() ?: "",

        viewCount = 0
    )
    constructor(group: Group, viewCount: Long) : this(
        id = group.id,
        title = group.title,
        description = group.description,
        memberId = group.member.id!!,
        author = group.member.nickname,
        maxParticipants = group.maxParticipants,
        status = group.status,
        category = group.groupCategories.map { CategoryResponseDto(it.category) },
        createdAt = group.createdAt?.toString() ?: "",
        modifiedAt = group.modifiedAt?.toString() ?: "",
        viewCount = viewCount
    )
}
