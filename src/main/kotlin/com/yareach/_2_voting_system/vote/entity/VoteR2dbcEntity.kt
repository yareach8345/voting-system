package com.yareach._2_voting_system.vote.entity

import com.yareach._2_voting_system.vote.model.Vote
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("vote")
class VoteR2dbcEntity(
    @Id
    val id: Int?,

    @Column("election_id")
    val electionId: String,

    @Column("user_id")
    val userId: String,

    val item: String,

    @Column("voted_at")
    val votedAt: LocalDateTime
) {
    companion object {
        fun fromModel(vote: Vote) = VoteR2dbcEntity(
            id = vote.id,
            electionId = vote.electionId,
            userId = vote.userId,
            item = vote.item,
            votedAt = vote.votedAt
        )
    }

    fun toModel() = Vote(id, electionId, userId, item, votedAt)
}