package com.yareach._2_voting_system.core.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionController {

    @ExceptionHandler(ApiException::class)
    fun apiException(exception: ApiException) =
        ErrorResponseDto
            .fromApiException(exception)
            .let{ ResponseEntity(it, it.state) }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(exception: MethodArgumentNotValidException) =
        ApiException(ErrorCode.VALIDATION_FAILED, exception.message ?: "")
            .let { ErrorResponseDto.fromApiException(it) }
            .let { ResponseEntity(it, HttpStatus.BAD_REQUEST) }
}