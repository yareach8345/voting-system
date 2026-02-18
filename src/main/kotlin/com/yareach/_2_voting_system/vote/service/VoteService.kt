package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.validation.Validator
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface VoteService {
    suspend fun record(electionId: String, userId: String, item: String): Vote

    suspend fun cancel(electionId: String, userId: String)

    suspend fun changeItem(electionId: String, userId: String, newItem: String)

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

    override suspend fun record(electionId: String, userId: String, item: String): Vote {

        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        if(!vote.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        if(!itemValidator.isValid(item)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "item $item is not valid.")
        }

        if(!userIdValidator.isValid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_USERID, "userId $userId is not valid.")
        }

        return voteRepository.insert(Vote.of(electionId, userId, item))
    }

    override suspend fun cancel(electionId: String, userId: String) {
        if(!userIdValidator.isValid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_USERID, "userId $userId is not valid.")
        }

        voteRepository.findByElectionIdAndUserId(electionId, userId) ?: throw ApiException(
            ErrorCode.VOTE_NOT_FOUND,
            "electionId: $electionId, userId: $userId is not found."
        )

        voteRepository.deleteByElectionIdAndUserId(electionId, userId)
    }

    override suspend fun changeItem(electionId: String, userId: String, newItem: String) {
        if(!userIdValidator.isValid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_USERID, "userId $userId is not valid.")
        }

        if(!itemValidator.isValid(newItem)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "item $newItem is not valid.")
        }

        voteRepository.findByElectionIdAndUserId(electionId, userId)
            ?.apply { updateItem(newItem) }
            ?.also { voteRepository.update(it) }
            ?: throw ApiException(ErrorCode.VOTE_NOT_FOUND, "electionId: $electionId, userId: $userId is not found.")
    }

    override suspend fun deleteByElectionId(electionId: String): Long {
        val vote = electionRepository.findById(electionId)
        if(vote === null) {
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