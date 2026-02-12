package com.yareach._2_voting_system.vote.dto.response

import java.time.LocalDateTime

data class VoteStateChangeResponse(
    val voteId: String,
    val newState: String,
    val updatedTime: LocalDateTime,
)