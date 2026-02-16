package com.yareach._2_voting_system.election.entity

import com.yareach._2_voting_system.election.model.Election
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("election")
class ElectionR2dbcEntity(
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

    fun toModel() = Election(id, isOpen, startedAt, endedAt, createdAt, lastModified)

    companion object {
        fun fromModel(electionModel: Election, isNewRecord: Boolean = false) = ElectionR2dbcEntity(
            id = electionModel.id,
            isOpen = electionModel.isOpen,
            startedAt = electionModel.startedAt,
            endedAt = electionModel.endedAt,
            createdAt = electionModel.createdAt,
            lastModified = electionModel.lastModified,
        ).apply { this.isNewRecord = isNewRecord }
    }
}