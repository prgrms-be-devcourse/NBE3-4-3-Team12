package com.example.backend.domain.voter.exception

import org.springframework.http.HttpStatus

class VoterException(
    val voterErrorCode: VoterErrorCode
) : RuntimeException(voterErrorCode.message) {

    fun getStatus(): HttpStatus = voterErrorCode.httpStatus

    fun getCode(): String = voterErrorCode.code
}
