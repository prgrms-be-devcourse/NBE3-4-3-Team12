package com.example.backend.domain.chat

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.member.entity.Member
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 채팅 메시지를 관리하는 서비스 클래스
 */
@Service
class ChatMessageService(
    private val chatMessagePublisher: ChatMessagePublisher // Redis Pub/Sub을 통한 메시지 발행기
) {

    fun sendMessage(group: Group, member: Member, content: String) {
        val message = ChatMessage(
            id = 0L, // 데이터베이스 저장 전에는 ID가 없으므로 0L 설정
            groupId = group.id, // 메시지가 속한 그룹의 ID
            senderId = member.id!!, // 메시지를 보낸 사용자의 ID
            content = content, // 메시지 내용
            createdAt = LocalDateTime.now() // 현재 시간으로 메시지 생성 시간 설정
        )
        chatMessagePublisher.publish(message) // 메시지를 발행하여 WebSocket을 통해 전송
    }
}
