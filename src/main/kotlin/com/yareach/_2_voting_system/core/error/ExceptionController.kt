package com.yareach._2_voting_system.core.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionController {

    @ExceptionHandler(ApiException::class)
    fun apiException(exception: ApiException) =
        ErrorResponseDto
            .fromApiException(exception)
            .let{ ResponseEntity(it, it.state) }
}