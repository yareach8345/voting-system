package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.error.exception.IllegalStateException
import com.yareach._2_voting_system.core.error.exception.NotFoundException
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface VoteService {
    suspend fun createNewVote(): String

    suspend fun deleteVote(voteId: String)

    suspend fun getAllVotes(): Flow<Vote>

    suspend fun getVote(voteId: String): Vote

    suspend fun openVote(voteId: String): Vote

    suspend fun closeVote(voteId: String): Vote

    suspend fun changeVoteState(voteId: String, newState: String): Vote

    suspend fun deleteExpiredVotes(): Long
}

@Service
class VoteServiceImpl(
    private val voteRepository: VoteRepository,
    @Value($$"${vote.expire.ttl-seconds:600}") private val ttlSeconds: Long = 600
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
        return voteRepository.findById(voteId) ?: throw NotFoundException(ErrorCode.VOTE_NOT_FOUND, voteId)
    }

    override suspend fun openVote(voteId: String): Vote {
        return voteRepository.modify(voteId) { open() }
    }

    override suspend fun closeVote(voteId: String): Vote {
        return voteRepository.modify(voteId) { close() }
    }

    override suspend fun changeVoteState(voteId: String, newState: String) = when (newState) {
        "open" -> openVote(voteId)
        "close" -> closeVote(voteId)
        else -> throw IllegalStateException(ErrorCode.ILLEGAL_VOTE_STATE, newState)
    }

    override suspend fun deleteExpiredVotes(): Long {
        val cutoff = LocalDateTime.now().minusSeconds(ttlSeconds)
        return voteRepository.deleteVotesBeforeCutoff(cutoff)
    }
}