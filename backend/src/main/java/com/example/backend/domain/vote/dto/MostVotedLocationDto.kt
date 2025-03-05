package com.example.backend.domain.vote.dto

data class MostVotedLocationDto(
    val address: String,
    val latitude: Double,
    val longitude: Double
)