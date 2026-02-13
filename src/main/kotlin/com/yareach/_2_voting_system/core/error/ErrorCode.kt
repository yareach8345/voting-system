package com.yareach._2_voting_system.core.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val state: HttpStatus,
    val message: String,
    val errorCode: String,
    val details: String
) {
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "투표를 찾을 수 없습니다.", "VOTE_NOT_FOUND", "ID가 [%s]인 투표를 찾을 수 없습니다."),
    ILLEGAL_VOTE_STATE(HttpStatus.BAD_GATEWAY, "올바른 상태가 아닙니다.", "ILLEGAL_VOTE_STATE", "[%s]는 옳은 상태가 아닙니다."),

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생했습니다.", "SERVER_ERROR", "개발자에게 문의 하세요")
}