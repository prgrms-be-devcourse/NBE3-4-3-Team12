package com.example.backend.domain.group.repository

import com.example.backend.domain.group.entity.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<Group, Long> {
    fun findGroupByMemberId(memberId: Long): List<Group>

    @Query("SELECT g FROM Group g WHERE g.member.id = :memberId AND g.status = 'COMPLETED'")
    fun findCompletedGroupsByMemberId(@Param("memberId") memberId: Long): List<Group>
}
