package com.example.backend.domain.chat

import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.repository.MemberRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


/**
 * 채팅방 관련 요청을 처리하는 컨트롤러
 */

@RestController
@RequestMapping("/api/groups")
class ChatRoomController(
    private val chatRoomService: ChatRoomService,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val groupMemberRepository: GroupMemberRepository
) {
    // 그룹 ID를 기반으로 채팅방을 생성하는 API
    @PostMapping("/{groupId}/chat-room")
    fun createChatRoom(@PathVariable groupId: Long): ResponseEntity<ChatRoom> {
        val chatRoom = chatRoomService.createRoom(groupId)
        return ResponseEntity.ok(chatRoom)
    }

    @GetMapping("/{groupId}/chat-room")
    fun findRoomById(@PathVariable groupId: Long): ResponseEntity<ChatRoom?> {
        val chatRoom = chatRoomService.findRoomById(groupId)
        return ResponseEntity.ok(chatRoom)
    }

    @GetMapping("/{groupId}/isMember")
    fun isMemberInGroup(@PathVariable groupId: Long, @RequestParam memberId: Long): ResponseEntity<Boolean> {

        val group = groupRepository.findById(groupId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없음") }

        val member = memberRepository.findById(memberId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없음") }

        val isMember = groupMemberRepository.existsByGroupAndMember(group, member)
        return ResponseEntity.ok(isMember)
    }
}
