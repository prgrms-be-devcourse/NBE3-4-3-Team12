package com.example.backend.domain.voter.exception

import org.springframework.http.HttpStatus

class VoterException(private val voterErrorCode: VoterErrorCode) : RuntimeException() {

    override val message: String
        get() = voterErrorCode.message

    val status: HttpStatus
        get() = voterErrorCode.httpStatus

    val code: String
        get() = voterErrorCode.code
}
