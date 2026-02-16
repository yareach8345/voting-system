package com.yareach._2_voting_system.core.error.exception

import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.error.ErrorResponse

data class ServerErrorException(
    val detail: String
): RuntimeException(detail) {
    fun toResponse() = ErrorResponse(
        state = ErrorCode.SERVER_ERROR.state,
        message = ErrorCode.SERVER_ERROR.message,
        code = ErrorCode.SERVER_ERROR.errorCode,
        details = detail
    )
}
