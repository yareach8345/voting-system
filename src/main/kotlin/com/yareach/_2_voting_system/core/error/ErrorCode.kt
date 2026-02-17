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
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "투표기록을 찾을 수 없습니다.", "VOTE_002"),
    NOT_VALID_ITEM(HttpStatus.BAD_REQUEST, "아이템이 유효하지 않습니다.", "VOTE_003"),

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청으로 보낸 데이터가 잘못되었습니다.", "VALID_ERROR"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생했습니다.", "SERVER_ERROR")
}