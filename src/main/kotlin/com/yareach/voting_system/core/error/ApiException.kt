package com.yareach.voting_system.core.error

data class ApiException(
    val errorCode: ErrorCode,
    val detail: String
) : RuntimeException(detail)