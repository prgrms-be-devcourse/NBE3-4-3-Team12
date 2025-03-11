package com.example.backend.domain.chat

import com.example.backend.domain.group.entity.Group
import org.springframework.data.repository.CrudRepository

interface ChatRoomRepository : CrudRepository<ChatRoom, Long> {
    fun findByGroup(group: Group): ChatRoom?
}
