package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service

interface VoteService {
    suspend fun createNewVote(): String

    suspend fun deleteVote(voteId: String)

    suspend fun getAllVotes(): Flow<Vote>

    suspend fun getVote(voteId: String): Vote

    suspend fun openVote(voteId: String)

    suspend fun closeVote(voteId: String)
}

@Service
class VoteServiceImpl(
    private val voteRepository: VoteRepository
) : VoteService {
    override suspend fun createNewVote(): String {
        val newVote = Vote.new()
        voteRepository.insert(newVote)
        return newVote.id
    }

    override suspend fun deleteVote(voteId: String) {
        voteRepository.deleteById(voteId)
    }

    override suspend fun getAllVotes(): Flow<Vote> {
        return voteRepository.findAll()
    }

    override suspend fun getVote(voteId: String): Vote {
        return voteRepository.findById(voteId) ?: throw NotFoundException()
    }

    override suspend fun openVote(voteId: String) {
        voteRepository.modify(voteId) { open() }
    }

    override suspend fun closeVote(voteId: String) {
        voteRepository.modify(voteId) { close() }
    }
}