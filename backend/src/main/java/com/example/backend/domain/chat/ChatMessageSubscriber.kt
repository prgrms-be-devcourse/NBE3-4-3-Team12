package com.example.backend.domain.chat

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.CopyOnWriteArraySet


/**
 * Redis Pub/Sub을 사용하여 채팅 메시지를 구독하는 클래스
 */

@Service
class ChatMessageSubscriber : MessageListener {

    companion object {
        private val sessions = CopyOnWriteArraySet<WebSocketSession>()
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val chatMessage = String(message.body)

        // 저장된 WebSocket 세션들에게 메시지 전송
        for (session in sessions) {
            if (session.isOpen) {
                session.sendMessage(TextMessage(chatMessage))
            }
        }
    }

}
