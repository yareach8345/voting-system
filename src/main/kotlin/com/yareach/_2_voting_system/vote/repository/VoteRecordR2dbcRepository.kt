package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteRecordR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRecordR2dbcRepository: CoroutineCrudRepository<VoteRecordR2dbcEntity, Int> {
    suspend fun findAllByVoteId(voteId: String): Flow<VoteRecordR2dbcEntity>

    suspend fun deleteAllByVoteId(voteId: String): Long

    suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String)
}