package com.yareach.voting_system.vote.dto

data class RecordVoteRequestDto(
    val userId: String,
    val item: String,
)