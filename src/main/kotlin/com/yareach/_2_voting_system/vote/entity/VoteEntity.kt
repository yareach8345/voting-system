package com.yareach._2_voting_system.vote.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("vote")
class VoteEntity(
    @Id
    var id: String,

    @Column("is_open")
    val isOpen: Boolean? = null,

    @Column("started_at")
    val startedAt: LocalDateTime? = null,

    @Column("ended_at")
    val endedAt: LocalDateTime? = null,

    @Column("created_at")
    val createdAt: LocalDateTime? = null,
)