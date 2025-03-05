package com.example.backend.domain.group.dto

import com.example.backend.domain.group.entity.GroupStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GroupModifyRequestDto(
    @field:NotBlank(message = "타이틀은 필수 항목입니다.")
    val title: String,
    @field:NotBlank(message = "설명은 필수 항목입니다.")
    val description: String,
    @field:NotNull(message = "인원은 필수 항목입니다.")
    val maxParticipants: Int,
    @field:NotNull(message = "모집상태는 필수 항목입니다.")
    val groupStatus: GroupStatus
)
