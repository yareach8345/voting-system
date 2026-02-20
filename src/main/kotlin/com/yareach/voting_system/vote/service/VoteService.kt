package com.yareach.voting_system.vote.service

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.core.validation.Validator
import com.yareach.voting_system.vote.model.Vote
import com.yareach.voting_system.vote.repository.VoteRepository
import com.yareach.voting_system.election.repository.ElectionRepository
import com.yareach.voting_system.vote.dto.ItemAndVotesCountPairDto
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface VoteService {
    suspend fun record(electionId: String, userId: String, item: String): Vote

    suspend fun getVoteInfo(electionId: String, userId: String): Vote

    suspend fun cancel(electionId: String, userId: String)

    suspend fun changeItem(electionId: String, userId: String, newItem: String): Vote

    suspend fun deleteByElectionId(electionId: String): Long

    suspend fun getElectionStatistics(electionId: String): Flow<ItemAndVotesCountPairDto>
}

@Service
class VoteServiceImpl(
    private val voteRepository: VoteRepository,
    private val electionRepository: ElectionRepository,
    @Qualifier("ItemValidator") private val itemValidator: Validator,
    @Qualifier("UserIdValidator") private val userIdValidator: Validator,
) : VoteService {

    fun validateItem(item: String) {
        if(!itemValidator.isValid(item)) {
            throw ApiException(ErrorCode.INVALID_ITEM, "item $item is not valid.")
        }
    }

    fun validateUserId(userId: String) {
        if(!userIdValidator.isValid(userId)) {
            throw ApiException(ErrorCode.INVALID_USERID, "userId $userId is not valid.")
        }
    }

    override suspend fun record(electionId: String, userId: String, item: String): Vote {
        validateItem(item)
        validateUserId(userId)

        val election = electionRepository.findById(electionId)
        if(election === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        if(!election.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        return voteRepository.insert(Vote.of(electionId, userId, item))
    }

    override suspend fun getVoteInfo(electionId: String, userId: String): Vote {
        return voteRepository.findByElectionIdAndUserId(electionId, userId)
            ?: throw ApiException(ErrorCode.VOTE_NOT_FOUND, "voteId $electionId not found.")
    }

    override suspend fun cancel(electionId: String, userId: String) {
        val election = electionRepository.findById(electionId)
        if(election === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }
        if(!election.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        validateUserId(userId)

        voteRepository.findByElectionIdAndUserId(electionId, userId) ?: throw ApiException(
            ErrorCode.VOTE_NOT_FOUND,
            "electionId: $electionId, userId: $userId is not found."
        )

        voteRepository.deleteByElectionIdAndUserId(electionId, userId)
    }

    override suspend fun changeItem(electionId: String, userId: String, newItem: String): Vote {
        validateItem(newItem)
        validateUserId(userId)

        val election = electionRepository.findById(electionId)
        if(election === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }
        if(!election.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        return voteRepository.findByElectionIdAndUserId(electionId, userId)
            ?.apply { updateItem(newItem) }
            ?.also { voteRepository.update(it) }
            ?: throw ApiException(ErrorCode.VOTE_NOT_FOUND, "electionId: $electionId, userId: $userId is not found.")
    }

    override suspend fun deleteByElectionId(electionId: String): Long {
        val election = electionRepository.findById(electionId)
        if(election === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        return voteRepository.deleteAllByElectionId(electionId)
    }

    override suspend fun getElectionStatistics(electionId: String): Flow<ItemAndVotesCountPairDto> {
        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        return voteRepository.getVoteCountsByElectionId(electionId)
    }
}