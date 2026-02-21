package com.yareach.voting_system.core.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val state: HttpStatus,
    val message: String,
    val errorCode: String,
) {
    ELECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Election Not Found.", "ELECTION_001"),
    INVALID_ELECTION_STATE(HttpStatus.BAD_REQUEST, "Invalid Election State.", "ELECTION_002"),

    ELECTION_IS_NOT_OPEN(HttpStatus.CONFLICT, "Election Is Not Open.", "VOTE_001"),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Vote Not Found.", "VOTE_002"),
    INVALID_ITEM(HttpStatus.BAD_REQUEST, "Invalid Item", "VOTE_003"),
    INVALID_USERID(HttpStatus.BAD_REQUEST, "Invalid UserId", "VOTE_004"),

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation Failed", "VALID_ERROR"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An Unexcepted Error Occurred", "SERVER_ERROR"),
    CONFIG_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Configuration Error", "PROP_ERROR"),
    PAGING_ERROR(HttpStatus.BAD_REQUEST, "Invalid Paging Parameters", "PAGING_ERROR"),
}