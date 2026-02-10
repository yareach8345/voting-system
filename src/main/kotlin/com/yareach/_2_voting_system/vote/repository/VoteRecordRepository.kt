package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteRecordEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRecordRepository: CoroutineCrudRepository<VoteRecordEntity, Int> {
    suspend fun findAllByVoteId(voteId: String): Flow<VoteRecordEntity>

    suspend fun deleteAllByVoteId(voteId: String): Long
}