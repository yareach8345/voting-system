package com.yareach.voting_system.election.model

import java.time.LocalDateTime
import java.util.UUID

class Election(
    val id: String,
    var isOpen: Boolean = false,
    var startedAt: LocalDateTime? = null,
    var endedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var lastModified: LocalDateTime = LocalDateTime.now(),
) {
    fun open() {
        isOpen = true
        startedAt = LocalDateTime.now()
    }

    fun close() {
        isOpen = false
        endedAt = LocalDateTime.now()
    }

    companion object {
        fun new() = Election(id = UUID.randomUUID().toString())
    }
}