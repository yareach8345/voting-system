package com.yareach._2_voting_system.model

import java.time.LocalDateTime
import java.util.UUID

class Vote(
    val id: String,
    var isOpen: Boolean = false,
    var startedAt: LocalDateTime? = null,
    var endedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
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
        fun new() = Vote(id = UUID.randomUUID().toString())
    }
}