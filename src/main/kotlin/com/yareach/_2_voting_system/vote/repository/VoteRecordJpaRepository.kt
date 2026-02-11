package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteRecordJpaEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRecordJpaRepository: CoroutineCrudRepository<VoteRecordJpaEntity, Int> {
    suspend fun findAllByVoteId(voteId: String): Flow<VoteRecordJpaEntity>

    suspend fun deleteAllByVoteId(voteId: String): Long

    suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String)
}