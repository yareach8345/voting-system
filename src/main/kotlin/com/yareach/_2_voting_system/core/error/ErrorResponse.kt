package com.yareach._2_voting_system.core.error

import org.springframework.http.HttpStatus

data class ErrorResponse(
    val state: HttpStatus,
    val message: String,
    val code: String,
    val details: String
)