package com.example.backend.domain.member.exception

import org.springframework.http.HttpStatus

/**
 * MemberErrorCode
 * 도메인 Member 커스텀 에러
 * @author 100minha
 */
enum class MemberErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "사용자를 찾을 수 없습니다.");

}
