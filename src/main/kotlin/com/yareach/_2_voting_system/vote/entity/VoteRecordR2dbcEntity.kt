package com.yareach._2_voting_system.vote.entity

import com.yareach._2_voting_system.vote.model.VoteRecord
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("vote")
class VoteRecordR2dbcEntity(
    @Id
    val id: Int?,

    @Column("election_id")
    val voteId: String,

    @Column("user_id")
    val userId: String,

    val item: String,

    @Column("voted_at")
    val votedAt: LocalDateTime
) {
    companion object {
        fun fromModel(voteRecord: VoteRecord) = VoteRecordR2dbcEntity(
            id = voteRecord.id,
            voteId = voteRecord.voteId,
            userId = voteRecord.userId,
            item = voteRecord.item,
            votedAt = voteRecord.votedAt
        )
    }

    fun toModel() = VoteRecord(id, voteId, userId, item, votedAt)
}