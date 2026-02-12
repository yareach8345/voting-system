package com.yareach._2_voting_system.vote.dto.response

import com.yareach._2_voting_system.vote.model.Vote
import java.time.LocalDateTime

data class VoteInfoResponse(
    val id: String,
    var state: String,
    var startedAt: LocalDateTime?,
    var endedAt: LocalDateTime?,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun fromVote(vote: Vote) = VoteInfoResponse(
            id = vote.id,
            state = if(vote.isOpen) "open" else "close",
            startedAt = vote.startedAt,
            endedAt = vote.endedAt,
            createdAt = vote.createdAt
        )
    }
}