package com.example.backend.domain.group.repository

import com.example.backend.domain.group.entity.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<Group, Long> {
    fun findGroupByMemberId(memberId: Long): List<Group>
    fun description(description: String): MutableList<Group>
}
