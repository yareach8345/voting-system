package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.model.VoteRecord
import com.yareach._2_voting_system.vote.entity.VoteRecordJpaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository

interface VoteRecordRepository {
    suspend fun findAllByVoteId(voteId: String): Flow<VoteRecord>

    suspend fun deleteAllByVoteId(voteId: String): Long

    suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String)

    suspend fun insert(record: VoteRecord)

    suspend fun update(record: VoteRecord)
}

@Repository
class VoteRecordRepositoryJpaImpl(
    private val voteRecordJpaRepository: VoteRecordJpaRepository
): VoteRecordRepository {
    override suspend fun findAllByVoteId(voteId: String): Flow<VoteRecord> {
        return voteRecordJpaRepository.findAllByVoteId(voteId).map { it.toModel() }
    }

    override suspend fun deleteAllByVoteId(voteId: String): Long {
        return voteRecordJpaRepository.deleteAllByVoteId(voteId)
    }

    override suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String) {
        return voteRecordJpaRepository.deleteByVoteIdAndUserId(voteId, userId)
    }

    override suspend fun insert(record: VoteRecord) {
        val entity = VoteRecordJpaEntity.fromModel(record)
        voteRecordJpaRepository.save(entity)
    }

    override suspend fun update(record: VoteRecord) {
        val entity = VoteRecordJpaEntity.fromModel(record)
        voteRecordJpaRepository.save(entity)
    }
}