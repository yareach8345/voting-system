package com.yareach.voting_system.election.service

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.election.dto.ElectionCountsByStateDto
import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface ElectionService {
    suspend fun createNewElection(): String

    suspend fun deleteElection(id: String)

    suspend fun getAllElections(): Flow<Election>

    suspend fun getElectionsWithPage(page: Long, size: Long): Flow<Election>

    suspend fun getElection(id: String): Election

    suspend fun getNumberOfElections(): Long

    suspend fun getCountsByState(): ElectionCountsByStateDto

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
        if (!electionRepository.isExists(id)) {
            throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
        }
        electionRepository.deleteById(id)
    }

    override suspend fun getAllElections(): Flow<Election> {
        return electionRepository.findAll()
    }

    override suspend fun getElectionsWithPage(page: Long, size: Long): Flow<Election> {
        if(page < 0) {
            throw ApiException(ErrorCode.PAGING_ERROR, "page 값이 잘못 되었습니다.")
        }
        if(size < 1) {
            throw ApiException(ErrorCode.PAGING_ERROR, "size는 1이상이어야 합니다.")
        }
        return electionRepository.findWithPaging(page, size)
    }

    override suspend fun getElection(id: String): Election {
        return electionRepository.findById(id) ?: throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
    }

    override suspend fun getNumberOfElections(): Long {
        return electionRepository.getNumberOfElections()
    }

    override suspend fun getCountsByState(): ElectionCountsByStateDto {
        val isOpenAndCountMap = electionRepository.countByIsOpen().toList().associate { it.isOpen to it.count }

        val openedCount = isOpenAndCountMap[true] ?: 0
        val closedCount = isOpenAndCountMap[false] ?: 0

        return ElectionCountsByStateDto(
            opened = openedCount,
            closed = closedCount,
            total = openedCount + closedCount
        )
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