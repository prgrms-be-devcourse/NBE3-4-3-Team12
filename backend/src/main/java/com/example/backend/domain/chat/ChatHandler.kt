package com.example.backend.domain.chat

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ChatHandler : TextWebSocketHandler() {

    private val sessionMap = mutableMapOf<String, WebSocketSession>() // 세션 ID 매핑

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessionMap[session.id] = session
        println("새 WebSocket 세션 연결됨: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("메시지 수신: ${message.payload}")

        // 특정 그룹 멤버들에게만 메시지 전송
        for ((_, s) in sessionMap) {
            s.sendMessage(message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessionMap.remove(session.id)
        println("WebSocket 세션 종료: ${session.id}")
    }
}
