package com.example.backend.domain.chat

import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.member.repository.MemberRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 채팅 메시지를 관리하는 컨트롤러
 */
@RestController
@RequestMapping("/chat/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService, // 채팅 메시지 관련 비즈니스 로직을 처리하는 서비스
    private val chatRoomService: ChatRoomService, // 채팅방 관리 서비스
    private val groupRepository: GroupRepository, // 그룹 정보를 관리하는 리포지토리
    private val memberRepository: MemberRepository // 멤버 정보를 관리하는 리포지토리
) {

    // 특정 그룹 내에서 멤버가 메시지를 전송하는 API
    @PostMapping("/{groupId}/{memberId}")
    fun sendMessage(
        @PathVariable groupId: Long, // 그룹 ID
        @PathVariable memberId: Long, // 멤버 ID
        @RequestBody messageRequest: ChatMessageRequestDto // 메시지 요청 데이터
    ): ResponseEntity<String> {
        // 그룹 조회
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("해당 그룹이 존재하지 않습니다.") }

        // 멤버 조회
        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("해당 멤버가 존재하지 않습니다.") }

        // 멤버가 해당 그룹의 일원인지 확인
        if (!chatRoomService.isMember(groupId, memberId)) {
            throw IllegalArgumentException("이 그룹의 멤버가 아닙니다.")
        }

        // 채팅 메시지 전송
        chatMessageService.sendMessage(group, member, messageRequest.content)
        return ResponseEntity.ok("Message sent successfully")
    }
}
