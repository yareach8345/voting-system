package com.yareach._2_voting_system.vote.dto

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
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
            val updatedTime = when(vote.isOpen) {
                true -> vote.startedAt ?: throw ApiException(ErrorCode.SERVER_ERROR, "started date is null")
                false -> vote.endedAt ?: throw ApiException(ErrorCode.SERVER_ERROR, "ended date is null")
            }

            return ChangeVoteStateResponseDto (
                voteId = vote.id,
                newState = newState,
                updatedTime = updatedTime
            )
        }
    }
}