package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteJpaEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VoteJpaRepository: CoroutineCrudRepository<VoteJpaEntity, String> {
    suspend fun deleteByLastModifiedBefore(pointInTime: LocalDateTime): Long
}