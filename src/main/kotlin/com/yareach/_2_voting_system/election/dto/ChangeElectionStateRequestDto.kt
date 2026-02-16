package com.yareach._2_voting_system.election.dto

import jakarta.validation.constraints.Pattern

data class ChangeElectionStateRequestDto (
    @Pattern(regexp = "open|close", message = "state는 open과 close 둘만 가능합니다.")
    val newState: String
)