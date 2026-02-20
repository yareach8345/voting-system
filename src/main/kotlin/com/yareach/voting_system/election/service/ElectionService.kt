package com.yareach.voting_system.election.service

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.election.entity.ElectionR2dbcEntity
import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionRepository
import kotlinx.coroutines.flow.Flow
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

    suspend fun deleteExpiredElections(cutoff: LocalDateTime): Long
}

@Service
class ElectionServiceImpl(
    private val electionRepository: ElectionRepository
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
        return electionRepository.findById(id)
            ?.apply { open() }
            ?.also{ electionRepository.update(it) }
            ?: throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
    }

    override suspend fun closeElection(id: String): Election {
        return electionRepository.findById(id)
            ?.apply { close() }
            ?.also{ electionRepository.update(it) }
            ?: throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
    }

    override suspend fun changeElectionState(id: String, newState: String) = when (newState) {
        "open" -> openElection(id)
        "close" -> closeElection(id)
        else -> throw ApiException(ErrorCode.INVALID_ELECTION_STATE, newState)
    }

    override suspend fun deleteExpiredElections(cutoff: LocalDateTime): Long {
        return electionRepository.deleteElectionsBeforeCutoff(cutoff)
    }
}