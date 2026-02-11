package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.model.Vote
import com.yareach._2_voting_system.vote.entity.VoteJpaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository

interface VoteRepository {
    suspend fun findAll(): Flow<Vote>

    suspend fun findById(voteId: String): Vote?

    suspend fun insert(vote: Vote): String

    suspend fun update(vote: Vote)

    suspend fun modify(voteId: String, block: Vote.() -> Unit)

    suspend fun deleteById(voteId: String)
}

@Repository
class VoteRepositoryJpaImpl(
    private val voteJpaRepository: VoteJpaRepository
) : VoteRepository {
    override suspend fun findAll(): Flow<Vote> {
        return voteJpaRepository.findAll().map { it.toModel() }
    }

    override suspend fun findById(voteId: String): Vote? {
        return voteJpaRepository.findById(voteId)?.toModel()
    }

    override suspend fun insert(vote: Vote): String {
        val voteEntity = VoteJpaEntity.fromModel(vote, isNewRecord = true)
        val result = voteJpaRepository.save(voteEntity)
        return result.id
    }

    override suspend fun update(vote: Vote) {
        val voteEntity = VoteJpaEntity.fromModel(vote)
        voteJpaRepository.save(voteEntity)
    }

    override suspend fun modify(voteId: String, block: Vote.() -> Unit) {
        voteJpaRepository.findById(voteId)
            ?.toModel()
            ?.apply { block() }
            ?.let { voteJpaRepository.save(VoteJpaEntity.fromModel(it)) }
            ?: throw Error("NOT FOUND")
    }

    override suspend fun deleteById(voteId: String) {
        voteJpaRepository.deleteById(voteId)
    }
}