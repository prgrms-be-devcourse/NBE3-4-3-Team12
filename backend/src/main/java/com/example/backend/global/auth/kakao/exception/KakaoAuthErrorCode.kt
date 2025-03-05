package com.example.backend.global.auth.kakao.exception

import lombok.AllArgsConstructor
import lombok.Getter
import org.springframework.http.HttpStatus

/**
 * KakaoAuthErrorCode
 * 카카오 서버와 통신 도중 발생할 수 있는 예외 코드 정리
 * @author 100minha
 */
enum class KakaoAuthErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {

    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "카카오 서버에서 알 수 없는 오류가 발생했습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "400-1", "잘못된 쿼리 파라미터가 설정되었습니다."),
    TOKEN_REISSUE_FAILED(HttpStatus.UNAUTHORIZED, "401-1", "토큰 갱신에 실패했습니다.");
}
