package com.example.backend.domain.member.exception

import org.springframework.http.HttpStatus

/**
 * MemberException
 * @author 100minha
 */
class MemberException(private val memberErrorCode: MemberErrorCode) : RuntimeException(memberErrorCode.name) {
    val status: HttpStatus
        get() = memberErrorCode.httpStatus

    val code: String
        get() = memberErrorCode.code
}
