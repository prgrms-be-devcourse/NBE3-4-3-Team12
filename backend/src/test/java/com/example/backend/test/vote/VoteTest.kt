package com.example.backend.test.vote

import com.example.backend.domain.vote.entity.Vote
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class VoteTest {
    @Test
    fun `Vote 엔티티 생성 테스트`() {
        // Given
        val groupId = 1L
        val location = "테스트 장소"
        val address = "테스트 주소"
        val latitude = 37.5665
        val longitude = 126.9780
        // When
        val vote = Vote(
            groupId = groupId,
            location = location,
            address = address,
            latitude = latitude,
            longitude = longitude
        )
        // Then
        assertNull(vote.id)
        assertEquals(groupId, vote.groupId)
        assertEquals(location, vote.location)
        assertEquals(address, vote.address)
        assertEquals(latitude, vote.latitude)
        assertEquals(longitude, vote.longitude)
    }
}