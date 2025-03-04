package com.example.backend.global.advice

import com.example.backend.domain.admin.exception.AdminException
import com.example.backend.domain.category.exception.CategoryException
import com.example.backend.domain.group.exception.GroupException
import com.example.backend.domain.member.exception.MemberException
import com.example.backend.domain.voter.exception.VoterException
import com.example.backend.global.exception.GlobalErrorCode
import com.example.backend.global.exception.GlobalException
import com.example.backend.global.response.ErrorResponse
import com.example.backend.global.response.ErrorResponse.Companion.of
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.function.Consumer

/**
 * GlobalControllerAdvice
 * Global 예외 처리 advice 클래스
 * @author 100minha
 */
// TODO: !! 연산자 사용 제거
@ControllerAdvice
class GlobalControllerAdvice {
    /**
     * 컨트롤러 @Valid 검증 실패 예외 핸들러 메소드
     * 컨트롤러 레이어에서
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors: MutableList<ErrorResponse.ValidationError> = ArrayList()

        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            errors.add(
                ErrorResponse.ValidationError(
                    (error as FieldError).field,
                    error.getDefaultMessage()
                )
            )
        })

        return ResponseEntity.status(ex.statusCode.value()).body(
            ErrorResponse.of(
                GlobalErrorCode.NOT_VALID.message,
                GlobalErrorCode.NOT_VALID.code,
                request.requestURI,
                errors
            )
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors: MutableList<ErrorResponse.ValidationError> = ArrayList()

        ex.constraintViolations.forEach(Consumer { violation: ConstraintViolation<*> ->
            val fieldName = violation.propertyPath.toString()
            val message = violation.message
            errors.add(ErrorResponse.ValidationError(fieldName, message))
        })

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.of(
                GlobalErrorCode.NOT_VALID.message,
                GlobalErrorCode.NOT_VALID.code,
                request.requestURI,
                errors
            )
        )
    }

    @ExceptionHandler(GroupException::class)
    fun handleGroupException(ex: GroupException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message!!, ex.code, request.requestURI))
    }

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(ex: GlobalException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message, ex.code, request.requestURI))
    }

    @ExceptionHandler(AdminException::class)
    fun handleAdminException(ex: AdminException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message!!, ex.code, request.requestURI))
    }

    @ExceptionHandler(CategoryException::class)
    fun handleCategoryException(ex: CategoryException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message!!, ex.code, request.requestURI))
    }

    @ExceptionHandler(MemberException::class)
    fun handleMemberException(ex: MemberException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message, ex.code, request.requestURI))
    }

    @ExceptionHandler(VoterException::class)
    fun handleVoterException(ex: VoterException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status)
            .body(ErrorResponse.of(ex.message!!, ex.code, request.requestURI))
    }

    @ExceptionHandler(Exception::class)
    fun handleVoterException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(ex.message ?: "INTERNAL_SERVER_ERROR", "500", request.requestURI))
    }
}
