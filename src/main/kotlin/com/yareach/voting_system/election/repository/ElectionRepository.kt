package com.yareach.voting_system.election.repository

import com.yareach.voting_system.election.dto.IsOpenAndCountPairDto
import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.entity.ElectionR2dbcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface ElectionRepository {
    suspend fun findAll(): Flow<Election>

    suspend fun findWithPaging(page: Long, size: Long): Flow<Election>

    suspend fun findById(id: String): Election?

    suspend fun isExists(id: String): Boolean

    suspend fun getIsOpen(id: String): Boolean?

    suspend fun getNumberOfElections(): Long

    suspend fun countByIsOpen(): Flow<IsOpenAndCountPairDto>

    suspend fun insert(election: Election): String

    suspend fun update(election: Election): Election

    suspend fun deleteById(id: String)

    suspend fun deleteElectionsBeforeCutoff(cutoff: LocalDateTime): Long

    suspend fun deleteAll()
}

@Repository
class ElectionRepositoryR2dbcImpl(
    private val electionR2DbcRepository: ElectionR2dbcRepository
) : ElectionRepository {
    override suspend fun findAll(): Flow<Election> {
        return electionR2DbcRepository.findAll().map { it.toModel() }
    }

    override suspend fun findWithPaging(page: Long, size: Long): Flow<Election> {
        return electionR2DbcRepository.findWithPaging(page, size).map { it.toModel() }
    }

    override suspend fun findById(id: String): Election? {
        return electionR2DbcRepository.findById(id)?.toModel()
    }

    override suspend fun isExists(id: String): Boolean {
        return electionR2DbcRepository.existsById(id)
    }

    override suspend fun getIsOpen(id: String): Boolean? {
        return electionR2DbcRepository.getIsOpenBy(id)
    }

    override suspend fun getNumberOfElections(): Long {
        return electionR2DbcRepository.count()
    }

    override suspend fun countByIsOpen(): Flow<IsOpenAndCountPairDto> {
        return electionR2DbcRepository.countByIsOpen()
    }

    override suspend fun insert(election: Election): String {
        val electionEntity = ElectionR2dbcEntity.fromModel(election, isNewRecord = true)
        val result = electionR2DbcRepository.save(electionEntity)
        return result.id
    }

    override suspend fun update(election: Election): Election {
        val electionEntity = ElectionR2dbcEntity.fromModel(election)
        return electionR2DbcRepository.save(electionEntity).toModel()
    }

    override suspend fun deleteById(id: String) {
        electionR2DbcRepository.deleteById(id)
    }

    override suspend fun deleteElectionsBeforeCutoff(cutoff: LocalDateTime): Long {
        return electionR2DbcRepository.deleteByLastModifiedBefore(cutoff)
    }

    override suspend fun deleteAll() {
        electionR2DbcRepository.deleteAll()
    }
}