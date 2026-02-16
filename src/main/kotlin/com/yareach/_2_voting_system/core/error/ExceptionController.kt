package com.yareach._2_voting_system.core.error

import com.yareach._2_voting_system.core.error.exception.IllegalStateException
import com.yareach._2_voting_system.core.error.exception.NotFoundException
import com.yareach._2_voting_system.core.error.exception.ServerErrorException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    fun notFoundExceptionHandler(exception: NotFoundException) =
        ResponseEntity(exception.toResponse(), HttpStatus.NOT_FOUND)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException::class)
    fun illegalStateExceptionHandler(exception: IllegalStateException) =
        ResponseEntity(exception.toResponse(), HttpStatus.BAD_REQUEST)

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServerErrorException::class)
    fun serverErrorExceptionHandler(exception: ServerErrorException) =
        ResponseEntity(exception.toResponse(), HttpStatus.INTERNAL_SERVER_ERROR)
}