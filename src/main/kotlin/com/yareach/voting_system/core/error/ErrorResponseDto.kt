package com.yareach.voting_system.core.error

import org.springframework.http.HttpStatus

data class ErrorResponseDto(
    val state: HttpStatus,
    val message: String,
    val errorCode: String,
    val detail: String
) {
    companion object {
        fun fromApiException(exception: ApiException): ErrorResponseDto {
            return ErrorResponseDto(
                state = exception.errorCode.state,
                message = exception.errorCode.message,
                errorCode = exception.errorCode.errorCode,
                detail = exception.detail
            )
        }
    }
}