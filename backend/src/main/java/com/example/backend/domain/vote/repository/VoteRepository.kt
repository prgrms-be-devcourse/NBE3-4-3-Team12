package com.example.backend.domain.vote.repository

import com.example.backend.domain.vote.entity.Vote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface VoteRepository : JpaRepository<Vote, Long> {
    fun findAllByGroupId(groupId: Long): List<Vote>

    // Optional로 반환 타입 변경 (Java 코드 호환성)
    fun findByIdAndGroupId(id: Long, groupId: Long): Optional<Vote>

    @Query("SELECT v.id FROM Vote v WHERE v.groupId = :groupId")
    fun findAllIdByGroupId(@Param("groupId") groupId: Long): List<Long>

    fun deleteAllByGroupId(groupId: Long)
}