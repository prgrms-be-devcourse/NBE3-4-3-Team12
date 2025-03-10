package com.example.backend.domain.vote.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MostVotedLocationDto(
    @JsonProperty("location") val location: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("latitude") val latitude: Double,
    @JsonProperty("longitude") val longitude: Double
)