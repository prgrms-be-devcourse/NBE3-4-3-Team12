package com.example.backend.domain.vote.service

import com.example.backend.domain.vote.dto.MostVotedLocationDto
import com.example.backend.domain.vote.dto.VoteRequestDto
import com.example.backend.domain.vote.dto.VoteResponseDto
import com.example.backend.domain.vote.dto.VoteResultDto
import com.example.backend.domain.vote.entity.Vote
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.repository.VoterRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val voterRepository: VoterRepository
) {
    @Transactional(readOnly = true)
    fun getMostVotedLocations(groupId: Long): VoteResultDto {
        // 1. groupId로 특정된 투표 모두 조회
        val votes = voteRepository.findAllByGroupId(groupId)

        // 2. 각 투표별 투표자수 계산 및 정렬
        val voteCountMap = votes.associateWith { vote ->
            voterRepository.countVoters(vote.id!!)
        }

        // 3. 최대 투표 찾기
        val maxVoteCount = voteCountMap.values.maxOrNull() ?: 0

        // 4. 최대 투표의 장소관련 필드들 찾기
        val topLocations = voteCountMap.entries
            .filter { it.value == maxVoteCount }
            .map { entry ->
                val vote = entry.key
                MostVotedLocationDto(
                    location = vote.location,
                    address = vote.address,
                    latitude = vote.latitude,
                    longitude = vote.longitude
                )
            }

        return VoteResultDto(
            mostVotedLocations = topLocations
        )
    }

    @Transactional
    fun createVote(groupId: Long, request: VoteRequestDto): VoteResponseDto {
        val vote = request.toEntity(groupId)
        val savedVote = voteRepository.save(vote)

        return VoteResponseDto.toDto(savedVote)
    }

    @Cacheable(value = ["votes"], key = "#groupId")
    fun findAllByGroupId(groupId: Long): List<VoteResponseDto> {
        return voteRepository.findAllByGroupId(groupId)
            .map { VoteResponseDto.toDto(it) }
    }

    fun findById(groupId: Long, voteId: Long): VoteResponseDto {
        val vote = voteRepository.findByIdAndGroupId(voteId, groupId)
            .orElseThrow { EntityNotFoundException("Vote not found") }
        return VoteResponseDto.toDto(vote)
    }

    @Transactional
    @CacheEvict(value = ["votes"], key = "#groupId")
    fun modifyVote(groupId: Long, voteId: Long, requestDto: VoteRequestDto): VoteResponseDto {
        // 존재 확인만 함
        voteRepository.findByIdAndGroupId(voteId, groupId)
            .orElseThrow { EntityNotFoundException("Vote not found") }

        // 영속성 save로 update수행
        val updatedVote = voteRepository.save(
            Vote(
                id = voteId,
                groupId = groupId,
                location = requestDto.location,
                address = requestDto.address,
                latitude = requestDto.latitude,
                longitude = requestDto.longitude
            )
        )

        return VoteResponseDto.toDto(updatedVote)
    }

    @CacheEvict(value = ["votes"], key = "#groupId")
    fun deleteVote(groupId: Long, voteId: Long) {
        val vote = voteRepository.findByIdAndGroupId(voteId, groupId)
            .orElseThrow { EntityNotFoundException("Vote not found") }

        voteRepository.delete(vote)
    }
}