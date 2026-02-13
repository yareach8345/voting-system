package com.yareach._2_voting_system.vote.dto.response

import com.yareach._2_voting_system.core.extension.ServerErrorException
import com.yareach._2_voting_system.vote.model.Vote
import java.time.LocalDateTime

data class VoteStateChangeResponse(
    val voteId: String,
    val newState: String,
    val updatedTime: LocalDateTime,
) {
    companion object {
        fun fromNewVoteModel(vote: Vote): VoteStateChangeResponse {
            val newState = if(vote.isOpen) "open" else "close"
            val updatedTime = if(vote.isOpen) vote.startedAt else vote.endedAt

            return VoteStateChangeResponse (
                voteId = vote.id,
                newState = newState,
                updatedTime = updatedTime ?: throw ServerErrorException("vote updated time is null")
            )
        }
    }
}