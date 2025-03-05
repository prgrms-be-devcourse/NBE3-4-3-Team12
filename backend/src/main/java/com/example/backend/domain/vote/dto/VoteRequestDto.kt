package com.example.backend.domain.vote.dto

import com.example.backend.domain.vote.entity.Vote

data class VoteRequestDto(
    val location: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toEntity(groupId: Long): Vote {
        return Vote(
            groupId = groupId,
            location = this.location,
            address = this.address,
            latitude = this.latitude,
            longitude = this.longitude
        )
    }
}
