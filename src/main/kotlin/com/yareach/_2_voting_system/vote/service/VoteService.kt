package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import com.yareach._2_voting_system.vote.validator.ItemValidator
import com.yareach._2_voting_system.vote.validator.UserIdValidator
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

interface VoteService {
    suspend fun record(electionId: String, userId: String, item: String)

    suspend fun cancel(electionId: String, userId: String)

    suspend fun changeItem(electionId: String, userId: String, newItem: String)

    suspend fun deleteByElectionId(electionId: String)

    suspend fun getElectionStatistics(electionId: String): Flow<ItemAndVotesCountPairDto>
}

@Service
class VoteServiceImpl(
    private val voteRepository: VoteRepository,
    private val electionRepository: ElectionRepository,
    private val itemValidator: ItemValidator,
    private val userIdValidator: UserIdValidator,
) : VoteService {

    override suspend fun record(electionId: String, userId: String, item: String) {

        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        if(!vote.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        if(!itemValidator.valid(item)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "item $item is not valid.")
        }

        if(!userIdValidator.valid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "userId $userId is not valid.")
        }

        voteRepository.insert(Vote.of(electionId, item, userId))
    }

    override suspend fun cancel(electionId: String, userId: String) {
        if(!userIdValidator.valid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "userId $userId is not valid.")
        }

        voteRepository.findByElectionIdAndUserId(electionId, userId) ?: throw ApiException(
            ErrorCode.VOTE_NOT_FOUND,
            "electionId: $electionId, userId: $userId is not found."
        )

        voteRepository.deleteByElectionIdAndUserId(electionId, userId)
    }

    override suspend fun changeItem(electionId: String, userId: String, newItem: String) {
        if(!userIdValidator.valid(userId)) {
            throw ApiException(ErrorCode.NOT_VALID_ITEM, "userId $userId is not valid.")
        }

        voteRepository.findByElectionIdAndUserId(electionId, userId)
            ?.apply { updateItem(newItem) }
            ?.also { voteRepository.update(it) }
            ?: throw ApiException(ErrorCode.VOTE_NOT_FOUND, "electionId: $electionId, userId: $userId is not found.")
    }

    override suspend fun deleteByElectionId(electionId: String) {
        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        voteRepository.deleteAllByElectionId(electionId)
    }

    override suspend fun getElectionStatistics(electionId: String): Flow<ItemAndVotesCountPairDto> {
        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        return voteRepository.getVoteCountsByElectionId(electionId)
    }
}