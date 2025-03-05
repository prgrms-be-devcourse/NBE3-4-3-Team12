package com.example.backend.domain.group.entity

import com.example.backend.domain.group.exception.GroupErrorCode
import com.example.backend.domain.group.exception.GroupException
import com.fasterxml.jackson.annotation.JsonCreator

enum class GroupStatus {
    RECRUITING,  // 모집 중
    NOT_RECRUITING,  // 모집 중단
    COMPLETED,  // 모집 완료
    VOTING,  // 투표중
    DELETED; // 삭제

    companion object {
        @JsonCreator
        fun from(value: String?): GroupStatus {
            for (status in entries) {
                if (status.name.equals(value, ignoreCase = true)) {
                    return status
                }
            }
            throw GroupException(GroupErrorCode.INCORRECT)
        }
    }
}
