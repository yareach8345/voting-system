package com.yareach._2_voting_system.core.extension

import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.error.ErrorResponse

data class IllegalStateException(
    val errorCode: ErrorCode,
    val wrongState: String,
    val detail: String = String.format(errorCode.details, wrongState)
): RuntimeException(detail) {
    fun toResponse() = ErrorResponse(
        state = errorCode.state,
        message = errorCode.message,
        code = errorCode.errorCode,
        details = detail
    )
}