package com.yareach._2_voting_system.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.vote.model.Election
import com.yareach._2_voting_system.vote.repository.ElectionRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface ElectionService {
    suspend fun createNewElection(): String

    suspend fun deleteElection(id: String)

    suspend fun getAllElections(): Flow<Election>

    suspend fun getElection(id: String): Election

    suspend fun openElection(id: String): Election

    suspend fun closeElection(id: String): Election

    suspend fun changeElectionState(id: String, newState: String): Election

    suspend fun deleteExpiredElections(): Long
}

@Service
class ElectionServiceImpl(
    private val electionRepository: ElectionRepository,
    @Value($$"${vote.expire.ttl-seconds:600}") private val ttlSeconds: Long = 600
) : ElectionService {
    override suspend fun createNewElection(): String {
        val newElection = Election.new()
        electionRepository.insert(newElection)
        return newElection.id
    }

    override suspend fun deleteElection(id: String) {
        electionRepository.deleteById(id)
    }

    override suspend fun getAllElections(): Flow<Election> {
        return electionRepository.findAll()
    }

    override suspend fun getElection(id: String): Election {
        return electionRepository.findById(id) ?: throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
    }

    override suspend fun openElection(id: String): Election {
        return electionRepository.modify(id) { open() }
    }

    override suspend fun closeElection(id: String): Election {
        return electionRepository.modify(id) { close() }
    }

    override suspend fun changeElectionState(id: String, newState: String) = when (newState) {
        "open" -> openElection(id)
        "close" -> closeElection(id)
        else -> throw ApiException(ErrorCode.ILLEGAL_ELECTION_STATE, newState)
    }

    override suspend fun deleteExpiredElections(): Long {
        val cutoff = LocalDateTime.now().minusSeconds(ttlSeconds)
        return electionRepository.deleteElectionsBeforeCutoff(cutoff)
    }
}