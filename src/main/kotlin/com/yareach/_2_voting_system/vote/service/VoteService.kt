package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.election.repository.ElectionRepository

interface VoteService {
    suspend fun record(voteId: String, userId: String, item: String)

    suspend fun deleteByVoteId(voteId: String)
}

class VoteServiceImpl(
    private val voteRepository: VoteRepository,
    private val electionRepository: ElectionRepository,
) : VoteService {

    override suspend fun record(voteId: String, userId: String, item: String) {

        val vote = electionRepository.findById(voteId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $voteId not found.")
        }

        if(!vote.isOpen) {
            throw ApiException(ErrorCode.ELECTION_IS_NOT_OPEN, "voteId $voteId is not open.")
        }

        voteRepository.insert(Vote.of(voteId, item, userId))
    }

    override suspend fun deleteByVoteId(voteId: String) {
        val vote = electionRepository.findById(voteId)
        if(vote === null) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "voteId $voteId not found.")
        }

        voteRepository.deleteAllByVoteId(voteId)
    }
}