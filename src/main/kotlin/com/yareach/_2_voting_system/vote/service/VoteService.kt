package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import kotlinx.coroutines.flow.Flow

interface VoteService {
    suspend fun record(electionId: String, userId: String, item: String)

    suspend fun deleteByElectionId(electionId: String)

    suspend fun getElectionStatistics(electionId: String): Flow<ItemAndVotesCountPairDto>
}

class VoteServiceImpl(
    private val voteRepository: VoteRepository,
    private val electionRepository: ElectionRepository,
) : VoteService {

    override suspend fun record(electionId: String, userId: String, item: String) {

        val vote = electionRepository.findById(electionId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $electionId not found.")
        }

        if(!vote.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $electionId is not open.")
        }

        voteRepository.insert(Vote.of(electionId, item, userId))
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

        return voteRepository.getNumberOfVotes(electionId)
    }
}