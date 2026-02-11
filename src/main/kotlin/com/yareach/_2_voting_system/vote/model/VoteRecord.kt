package com.yareach._2_voting_system.vote.model

import java.time.LocalDateTime

class VoteRecord(
    val id: Int? = null,
    val voteId: String,
    val userId: String,
    val item: String,
    val votedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun of(
            voteId: String,
            userId: String,
            item: String,
        ) = VoteRecord(id = null, voteId = voteId, userId = userId, item = item)
    }
}