package com.yareach.voting_system.election.dto

import java.time.LocalDateTime

data class GetNumberOfElectionsResponseDto(
    val count: Long,
    val aggregatedAt: LocalDateTime
) {
    companion object {
        fun fromNumberOfElections(numberOfElections: Long) = GetNumberOfElectionsResponseDto(
            count = numberOfElections,
            aggregatedAt = LocalDateTime.now()
        )
    }
}
