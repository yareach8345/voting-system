package com.yareach._2_voting_system.vote.service

interface VoteRecordService {
    suspend fun record(voteId: String, userId: String, item: String)

    suspend fun deleteByVoteId(id: String)
}

