package com.yareach._2_voting_system.election.repository

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.election.model.Election
import com.yareach._2_voting_system.election.entity.ElectionR2dbcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface ElectionRepository {
    suspend fun findAll(): Flow<Election>

    suspend fun findById(id: String): Election?

    suspend fun insert(election: Election): String

    suspend fun update(election: Election): Election

    suspend fun modify(id: String, block: Election.() -> Unit): Election

    suspend fun deleteById(id: String)

    suspend fun deleteElectionsBeforeCutoff(cutoff: LocalDateTime): Long
}

@Repository
class ElectionRepositoryR2DbcImpl(
    private val electionR2DbcRepository: ElectionR2dbcRepository
) : ElectionRepository {
    override suspend fun findAll(): Flow<Election> {
        return electionR2DbcRepository.findAll().map { it.toModel() }
    }

    override suspend fun findById(id: String): Election? {
        return electionR2DbcRepository.findById(id)?.toModel()
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

    override suspend fun modify(id: String, block: Election.() -> Unit): Election {
        return electionR2DbcRepository.findById(id)
            ?.toModel()
            ?.apply { block() }
            ?.also{ electionR2DbcRepository.save(ElectionR2dbcEntity.fromModel(it)) }
            ?: throw ApiException(ErrorCode.ELECTION_NOT_FOUND, "electionId: $id")
    }

    override suspend fun deleteById(id: String) {
        electionR2DbcRepository.deleteById(id)
    }

    override suspend fun deleteElectionsBeforeCutoff(cutoff: LocalDateTime): Long {
        return electionR2DbcRepository.deleteByLastModifiedBefore(cutoff)
    }
}