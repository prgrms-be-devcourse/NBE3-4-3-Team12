package com.example.backend.domain.vote.dto

import com.example.backend.domain.vote.entity.Vote
import java.time.LocalDateTime

data class VoteResponseDto(
    val id: Long?,
    val location: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun toDto(vote: Vote): VoteResponseDto {
            return VoteResponseDto(
                id = vote.id,
                location = vote.location,
                address = vote.address,
                latitude = vote.latitude,
                longitude = vote.longitude,
                createdAt = vote.createdAt
            )
        }
    }
}