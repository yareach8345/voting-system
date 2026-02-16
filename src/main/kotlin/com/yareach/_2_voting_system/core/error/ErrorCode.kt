package com.yareach._2_voting_system.core.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val state: HttpStatus,
    val message: String,
    val errorCode: String,
) {
    ELECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "투표를 찾을 수 없습니다.", "ELECTION_001"),
    ILLEGAL_ELECTION_STATE(HttpStatus.BAD_REQUEST, "올바른 상태가 아닙니다.", "ELECTION_002"),

    ELECTION_IS_NOT_OPEN(HttpStatus.BAD_REQUEST, "투표가 진행중이지 않습니다.", "VOTE_001"),

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생했습니다.", "SERVER_ERROR")
}