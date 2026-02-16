package com.yareach._2_voting_system.vote.dto

import com.yareach._2_voting_system.core.error.exception.ServerErrorException
import com.yareach._2_voting_system.vote.model.Vote
import java.time.LocalDateTime

data class ChangeVoteStateResponseDto(
    val voteId: String,
    val newState: String,
    val updatedTime: LocalDateTime,
) {
    companion object {
        fun fromNewVoteModel(vote: Vote): ChangeVoteStateResponseDto {
            val newState = if(vote.isOpen) "open" else "close"
            val updatedTime = if(vote.isOpen) vote.startedAt else vote.endedAt

            return ChangeVoteStateResponseDto (
                voteId = vote.id,
                newState = newState,
                updatedTime = updatedTime ?: throw ServerErrorException("vote updated time is null")
            )
        }
    }
}