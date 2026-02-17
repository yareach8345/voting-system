package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.entity.VoteR2dbcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository

interface VoteRepository {
    suspend fun findAllByElectionId(electionId: String): Flow<Vote>

    suspend fun findByElectionIdAndUserId(electionId: String, userId: String): Vote?

    suspend fun deleteAllByElectionId(electionId: String): Long

    suspend fun deleteByElectionIdAndUserId(electionId: String, userId: String): Long

    suspend fun insert(vote: Vote)

    suspend fun update(vote: Vote)

    suspend fun countByElectionId(electionId: String): Long

    suspend fun getVoteCountsByElectionId(electionId: String): Flow<ItemAndVotesCountPairDto>
}

@Repository
class VoteRepositoryR2dbcImpl(
    private val voteR2DbcRepository: VoteR2dbcRepository,
): VoteRepository {
    override suspend fun findAllByElectionId(electionId: String): Flow<Vote> {
        return voteR2DbcRepository.findAllByElectionId(electionId).map { it.toModel() }
    }

    override suspend fun findByElectionIdAndUserId(electionId: String, userId: String): Vote? {
        return voteR2DbcRepository.findByElectionIdAndUserId(electionId, userId)?.toModel()
    }

    override suspend fun deleteAllByElectionId(electionId: String): Long {
        return voteR2DbcRepository.deleteAllByElectionId(electionId)
    }

    override suspend fun deleteByElectionIdAndUserId(electionId: String, userId: String): Long {
        return voteR2DbcRepository.deleteByElectionIdAndUserId(electionId, userId)
    }

    override suspend fun insert(vote: Vote) {
        val entity = VoteR2dbcEntity.fromModel(vote)
        voteR2DbcRepository.save(entity)
    }

    override suspend fun update(vote: Vote) {
        val entity = VoteR2dbcEntity.fromModel(vote)
        voteR2DbcRepository.save(entity)
    }

    override suspend fun countByElectionId(electionId: String): Long {
        return voteR2DbcRepository.countByElectionId(electionId)
    }

    override suspend fun getVoteCountsByElectionId(electionId: String): Flow<ItemAndVotesCountPairDto> {
        return voteR2DbcRepository.countGroupByElectionId(electionId)
    }
}