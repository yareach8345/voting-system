package com.yareach._2_voting_system.vote.entity

import com.yareach._2_voting_system.vote.model.Vote
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("vote")
class VoteJpaEntity(
    @Id
    private val id: String,

    @Column("is_open")
    var isOpen: Boolean = false,

    @Column("started_at")
    var startedAt: LocalDateTime? = null,

    @Column("ended_at")
    var endedAt: LocalDateTime? = null,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("last_modified")
    var lastModified: LocalDateTime = LocalDateTime.now(),
): Persistable<String> {
    @Transient
    private var isNewRecord: Boolean = false

    override fun getId(): String = id

    override fun isNew(): Boolean = isNewRecord

    fun toModel() = Vote(id, isOpen, startedAt, endedAt, createdAt, lastModified)

    companion object {
        fun fromModel(voteModel: Vote, isNewRecord: Boolean = false) = VoteJpaEntity(
            id = voteModel.id,
            isOpen = voteModel.isOpen,
            startedAt = voteModel.startedAt,
            endedAt = voteModel.endedAt,
            createdAt = voteModel.createdAt,
            lastModified = voteModel.lastModified,
        ).apply { this.isNewRecord = isNewRecord }
    }
}