package com.yareach._2_voting_system.vote.dto

import com.yareach._2_voting_system.vote.model.Election
import java.time.LocalDateTime

data class ElectionInfoResponseDto(
    val id: String,
    var state: String,
    var startedAt: LocalDateTime?,
    var endedAt: LocalDateTime?,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun fromElection(election: Election) = ElectionInfoResponseDto(
            id = election.id,
            state = if(election.isOpen) "open" else "close",
            startedAt = election.startedAt,
            endedAt = election.endedAt,
            createdAt = election.createdAt
        )
    }
}