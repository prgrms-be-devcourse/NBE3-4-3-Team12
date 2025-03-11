package com.example.backend.domain.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByGroupId(groupId: Long): List<ChatMessage>
}
