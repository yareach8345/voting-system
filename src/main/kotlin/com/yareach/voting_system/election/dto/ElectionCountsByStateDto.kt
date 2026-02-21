package com.yareach.voting_system.election.dto

data class ElectionCountsByStateDto(
    val opened: Long,
    val closed: Long,
    val total: Long
)