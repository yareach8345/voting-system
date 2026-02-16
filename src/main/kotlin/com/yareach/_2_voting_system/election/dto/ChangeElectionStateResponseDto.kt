package com.yareach._2_voting_system.election.dto

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.election.model.Election
import java.time.LocalDateTime

data class ChangeElectionStateResponseDto(
    val electionId: String,
    val newState: String,
    val updatedTime: LocalDateTime,
) {
    companion object {
        fun fromNewElectionModel(election: Election): ChangeElectionStateResponseDto {
            val newState = if(election.isOpen) "open" else "close"
            val updatedTime = when(election.isOpen) {
                true -> election.startedAt ?: throw ApiException(ErrorCode.SERVER_ERROR, "started date is null")
                false -> election.endedAt ?: throw ApiException(ErrorCode.SERVER_ERROR, "ended date is null")
            }

            return ChangeElectionStateResponseDto (
                electionId = election.id,
                newState = newState,
                updatedTime = updatedTime
            )
        }
    }
}