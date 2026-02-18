package com.yareach.voting_system.election.repository

import com.yareach.voting_system.election.entity.ElectionR2dbcEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ElectionR2dbcRepository: CoroutineCrudRepository<ElectionR2dbcEntity, String> {
    suspend fun deleteByLastModifiedBefore(pointInTime: LocalDateTime): Long
}