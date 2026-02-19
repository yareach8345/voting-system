package com.yareach.voting_system.vote.dto

import java.time.LocalDateTime

data class VoteStatisticsResponseDto(
    val voteCounts: List<ItemAndVotesCountPairDto>,
    val aggregatedAt: LocalDateTime
) {
    companion object {
        fun from(pairDtoList: List<ItemAndVotesCountPairDto>, aggregatedAt: LocalDateTime): VoteStatisticsResponseDto {
            val statisticsMap = pairDtoList.sortedByDescending { it.voteCount }

            return VoteStatisticsResponseDto(
                statisticsMap,
                aggregatedAt
            )
        }
    }
}