package com.example.backend.domain.chat

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,  // UUID 직접 생성
    val groupId: Long,
    val senderId: Long,
    val content: String,
    val createdAt: LocalDateTime
)
