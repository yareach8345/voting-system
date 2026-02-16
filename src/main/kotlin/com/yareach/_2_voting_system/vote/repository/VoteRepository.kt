package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.entity.VoteR2dbcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository

interface VoteRepository {
    suspend fun findAllByVoteId(voteId: String): Flow<Vote>

    suspend fun deleteAllByVoteId(voteId: String): Long

    suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String)

    suspend fun insert(record: Vote)

    suspend fun update(record: Vote)
}

@Repository
class VoteRepositoryR2DbcImpl(
    private val voteR2DbcRepository: VoteR2dbcRepository
): VoteRepository {
    override suspend fun findAllByVoteId(voteId: String): Flow<Vote> {
        return voteR2DbcRepository.findAllByElectionId(voteId).map { it.toModel() }
    }

    override suspend fun deleteAllByVoteId(voteId: String): Long {
        return voteR2DbcRepository.deleteAllByElectionId(voteId)
    }

    override suspend fun deleteByVoteIdAndUserId(voteId: String, userId: String) {
        return voteR2DbcRepository.deleteByElectionIdAndUserId(voteId, userId)
    }

    override suspend fun insert(record: Vote) {
        val entity = VoteR2dbcEntity.fromModel(record)
        voteR2DbcRepository.save(entity)
    }

    override suspend fun update(record: Vote) {
        val entity = VoteR2dbcEntity.fromModel(record)
        voteR2DbcRepository.save(entity)
    }
}