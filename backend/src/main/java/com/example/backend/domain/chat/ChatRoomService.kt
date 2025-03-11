package com.example.backend.domain.chat

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.entity.Member
import com.example.backend.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class ChatRoomService(
    private val groupMemberRepository: GroupMemberRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
) {

    fun isMember(groupId: Long, memberId: Long): Boolean {
        val group: Group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("해당 그룹이 존재하지 않습니다.") }
        val member: Member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("해당 멤버가 존재하지 않습니다.") }
        return groupMemberRepository.existsByGroupAndMember(group, member)
    }

    fun createRoom(groupId: Long): ChatRoom {
        val group: Group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("해당 그룹이 존재하지 않습니다.") }
        val chatRoom = ChatRoom(group = group)  // 🔹 생성자 변경
        return chatRoomRepository.save(chatRoom)
    }

    fun findRoomById(groupId: Long): ChatRoom? {
        val group: Group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("해당 그룹이 존재하지 않습니다.") }
        return chatRoomRepository.findByGroup(group)  // 🔹 groupId -> Group 객체로 변경
    }
}
