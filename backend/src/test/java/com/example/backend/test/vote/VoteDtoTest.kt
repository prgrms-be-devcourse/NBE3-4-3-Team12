package com.example.backend.test.vote

import com.example.backend.domain.vote.dto.VoteRequestDto
import org.junit.jupiter.api.Test
import kotlin.math.log
import kotlin.test.assertEquals

class VoteDtoTest {

    @Test
    fun `VoteRequestDto toEntity 변환 테스트`() {
        // given
        val expectedLocation = "테스트 장소"
        val expectedAddress = "테스트 주소"
        val expectedLatitude = 35.55
        val expectedLongitude = 126.66
        val groupId = 1L

        val requestDto = VoteRequestDto(
            location = expectedLocation,
            address = expectedAddress,
            latitude = expectedLatitude,
            longitude = expectedLongitude
        )

        // when
        val entity = requestDto.toEntity(groupId)

        // then
        // Check correct groupId
        assertEquals(groupId, entity.groupId)

        // Check  mapping and value
        assertEquals(requestDto.location, entity.location)
        assertEquals(expectedLocation, entity.location)

        // Check address mapping and value
        assertEquals(requestDto.address, entity.address)
        assertEquals(expectedAddress, entity.address)

        // Check  mapping and value
        assertEquals(requestDto.latitude, entity.latitude)
        assertEquals(expectedLatitude, entity.latitude)

        // Check mapping and value
        assertEquals(requestDto.longitude, entity.longitude)
        assertEquals(expectedLongitude, entity.longitude)
    }
}