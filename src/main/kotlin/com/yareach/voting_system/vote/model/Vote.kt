package com.yareach.voting_system.vote.model

import java.time.LocalDateTime

class Vote(
    val id: Int? = null,
    val electionId: String,
    val userId: String,
    item: String,
    votedAt: LocalDateTime = LocalDateTime.now()
) {
    var item = item
        private set

    var votedAt: LocalDateTime = votedAt
        private set

    fun updateItem(newItem: String) {
        item = newItem
        votedAt = LocalDateTime.now()
    }

    companion object {
        fun of(
            electionId: String,
            userId: String,
            item: String,
        ) = Vote(id = null, electionId = electionId, userId = userId, item = item)
    }
}