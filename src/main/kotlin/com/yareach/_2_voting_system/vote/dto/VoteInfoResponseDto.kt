package com.yareach._2_voting_system.vote.dto

import com.yareach._2_voting_system.vote.model.Vote
import java.time.LocalDateTime

data class VoteInfoResponseDto (
    val electionId: String,
    val userId: String,
    val item: String,
    val votedAt: LocalDateTime
) {
    companion object {
        fun fromVote(vote: Vote) = VoteInfoResponseDto(
            electionId = vote.electionId,
            userId = vote.userId,
            item = vote.item,
            votedAt = vote.votedAt
        )
    }
}