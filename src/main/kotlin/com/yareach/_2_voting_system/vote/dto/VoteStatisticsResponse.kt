package com.yareach._2_voting_system.vote.dto

import java.time.LocalDateTime

data class VoteStatisticsResponse(
    val voteCounts: List<ItemAndVotesCountPairDto>,
    val aggregatedAt: LocalDateTime
) {
    companion object {
        fun from(pairDtoList: List<ItemAndVotesCountPairDto>, aggregatedAt: LocalDateTime): VoteStatisticsResponse {
            val statisticsMap = pairDtoList.sortedByDescending { it.voteCount }

            return VoteStatisticsResponse(
                statisticsMap,
                aggregatedAt
            )
        }
    }
}