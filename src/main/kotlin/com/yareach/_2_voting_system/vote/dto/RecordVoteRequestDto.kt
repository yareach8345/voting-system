package com.yareach._2_voting_system.vote.dto

data class RecordVoteRequestDto(
    val userId: String,
    val item: String,
)