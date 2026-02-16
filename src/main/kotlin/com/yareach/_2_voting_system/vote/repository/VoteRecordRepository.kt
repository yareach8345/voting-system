package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.model.VoteRecord
import com.yareach._2_voting_system.vote.entity.VoteRecordR2dbcEntity
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
class VoteRecordRepositoryR2dbcImpl(
    private val voteRecordR2dbcRepository: VoteRecordR2dbcRepository
): VoteRecordRepository {
    override suspend fun findAllByVoteId(voteId: String): Flow<VoteRecord> {
        return voteRecordR2dbcRepository.findAllByVoteId(voteId).map { it.toModel() }
    }

    override suspend fun deleteAllByVoteId(voteId: String): Long {
        return voteRecordR2dbcRepository.deleteAllByVoteId(voteId)
    }

    override suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String) {
        return voteRecordR2dbcRepository.deleteByVoteIdAndUserId(voteId, userId)
    }

    override suspend fun insert(record: VoteRecord) {
        val entity = VoteRecordR2dbcEntity.fromModel(record)
        voteRecordR2dbcRepository.save(entity)
    }

    override suspend fun update(record: VoteRecord) {
        val entity = VoteRecordR2dbcEntity.fromModel(record)
        voteRecordR2dbcRepository.save(entity)
    }
}