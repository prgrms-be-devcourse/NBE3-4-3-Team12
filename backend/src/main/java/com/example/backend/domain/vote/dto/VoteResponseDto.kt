package com.example.backend.domain.vote.dto

import com.example.backend.domain.vote.entity.Vote
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class VoteResponseDto(
    @JsonProperty("id")
    val id: Long?,
    @JsonProperty("location")
    val location: String,
    @JsonProperty("address")
    val address: String,
    @JsonProperty("latitude")
    val latitude: Double,
    @JsonProperty("longitude")
    val longitude: Double,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("createdAt")
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