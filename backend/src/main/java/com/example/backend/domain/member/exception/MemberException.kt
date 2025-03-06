package com.example.backend.domain.member.exception

import org.springframework.http.HttpStatus

/**
 * MemberException
 * @author 100minha
 */
class MemberException(private val memberErrorCode: MemberErrorCode) :
    RuntimeException() {

    override val message: String
        get() = memberErrorCode.message

    val status: HttpStatus
        get() = memberErrorCode.httpStatus

    val code: String
        get() = memberErrorCode.code
}
