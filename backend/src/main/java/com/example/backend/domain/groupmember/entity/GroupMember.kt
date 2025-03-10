package com.example.backend.domain.groupmember.entity

import com.example.backend.domain.group.entity.Group
import com.example.backend.domain.member.entity.Member
import com.example.backend.global.base.BaseEntity
import jakarta.persistence.*

@Entity
class GroupMember : BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    lateinit var member: Member


    @ManyToOne
    @JoinColumn(name = "groups_id", nullable = false)
    lateinit var group: Group

    @Enumerated(value = EnumType.STRING)
    private var groupMemberStatus: GroupMemberStatus? = null

    constructor(member: Member, group: Group, groupMemberStatus: GroupMemberStatus?) {
        this.member = member
        this.group = group
        this.groupMemberStatus = GroupMemberStatus.APPLYING
    }

    constructor(member: Member, group: Group) {
        this.member = member
        this.group = group
    }
}
