package com.example.backend.domain.groupmember.repository;

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.groupmember.entity.GroupMember;
import com.example.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
    fun existsByGroupAndMember(group : Group, member : Member) : Boolean
    fun countByGroup(group : Group) :Long
    fun findByMember(member : Member) : MutableList<GroupMember>
}
