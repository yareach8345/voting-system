package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.entity.VoteR2dbcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface VoteRepository {
    suspend fun findAll(): Flow<Vote>

    suspend fun findById(voteId: String): Vote?

    suspend fun insert(vote: Vote): String

    suspend fun update(vote: Vote): Vote

    suspend fun modify(voteId: String, block: Vote.() -> Unit): Vote

    suspend fun deleteById(voteId: String)

    suspend fun deleteVotesBeforeCutoff(cutoff: LocalDateTime): Long
}

@Repository
class VoteRepositoryR2dbcImpl(
    private val voteR2dbcRepository: VoteR2dbcRepository
) : VoteRepository {
    override suspend fun findAll(): Flow<Vote> {
        return voteR2dbcRepository.findAll().map { it.toModel() }
    }

    override suspend fun findById(voteId: String): Vote? {
        return voteR2dbcRepository.findById(voteId)?.toModel()
    }

    override suspend fun insert(vote: Vote): String {
        val voteEntity = VoteR2dbcEntity.fromModel(vote, isNewRecord = true)
        val result = voteR2dbcRepository.save(voteEntity)
        return result.id
    }

    override suspend fun update(vote: Vote): Vote {
        val voteEntity = VoteR2dbcEntity.fromModel(vote)
        return voteR2dbcRepository.save(voteEntity).toModel()
    }

    override suspend fun modify(voteId: String, block: Vote.() -> Unit): Vote {
        return voteR2dbcRepository.findById(voteId)
            ?.toModel()
            ?.apply { block() }
            ?.also{ voteR2dbcRepository.save(VoteR2dbcEntity.fromModel(it)) }
            ?: throw ApiException(ErrorCode.VOTE_NOT_FOUND, "voteId: $voteId")
    }

    override suspend fun deleteById(voteId: String) {
        voteR2dbcRepository.deleteById(voteId)
    }

    override suspend fun deleteVotesBeforeCutoff(cutoff: LocalDateTime): Long {
        return voteR2dbcRepository.deleteByLastModifiedBefore(cutoff)
    }
}