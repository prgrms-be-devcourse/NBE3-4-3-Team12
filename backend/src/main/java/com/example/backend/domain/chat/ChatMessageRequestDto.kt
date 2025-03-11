package com.example.backend.domain.chat


/**
 * 클라이언트가 채팅 메시지를 보낼 때 사용하는 DTO
 */

data class ChatMessageRequestDto(
    val groupId: Long,  // 그룹 ID (채팅방의 ID 역할)
    val senderId: Long, // 메시지를 보낸 사용자의 ID
    val content: String // 채팅 메시지 내용
)
