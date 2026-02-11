package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.vote.entity.VoteEntity
import com.yareach._2_voting_system.vote.repository.VoteRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface VoteService {
    suspend fun createNewVote(): String

    suspend fun deleteVote(voteId: String)

    suspend fun getAllVotes(): Flow<VoteEntity>

    suspend fun getVote(voteId: String): VoteEntity

    suspend fun openVote(voteId: String)

    suspend fun closeVote(voteId: String)
}

@Service
class VoteServiceImpl(
    private val voteRepository: VoteRepository
) : VoteService {
    override suspend fun createNewVote(): String {
        val newVoteEntity = VoteEntity.new()
        voteRepository.save(newVoteEntity)
        return newVoteEntity.id
    }

    override suspend fun deleteVote(voteId: String) {
        voteRepository.deleteById(voteId)
    }

    override suspend fun getAllVotes(): Flow<VoteEntity> {
        return voteRepository.findAll()
    }

    override suspend fun getVote(voteId: String): VoteEntity {
        return voteRepository.findById(voteId) ?: throw NotFoundException()
    }

    override suspend fun openVote(voteId: String) {
        val voteEntity = voteRepository.findById(voteId) ?: throw NotFoundException()

        voteEntity.isOpen = true
        voteEntity.startedAt = LocalDateTime.now()

        voteRepository.save(voteEntity)
    }

    override suspend fun closeVote(voteId: String) {
        val voteEntity = voteRepository.findById(voteId) ?: throw NotFoundException()

        voteEntity.isOpen = false
        voteEntity.endedAt = LocalDateTime.now()

        voteRepository.save(voteEntity)
    }
}