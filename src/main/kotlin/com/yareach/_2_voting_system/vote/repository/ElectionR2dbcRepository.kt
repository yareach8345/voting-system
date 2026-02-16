package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.ElectionR2dbcEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ElectionR2dbcRepository: CoroutineCrudRepository<ElectionR2dbcEntity, String> {
    suspend fun deleteByLastModifiedBefore(pointInTime: LocalDateTime): Long
}