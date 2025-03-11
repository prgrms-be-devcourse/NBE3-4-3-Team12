package com.example.backend.domain.chat

import com.example.backend.domain.group.entity.Group
import jakarta.persistence.*


/**
 * 채팅방 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "chat_room")
class ChatRoom(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L, // 채팅방의 ID

    @OneToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group
)
