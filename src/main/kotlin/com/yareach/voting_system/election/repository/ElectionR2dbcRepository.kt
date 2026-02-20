package com.yareach.voting_system.election.repository

import com.yareach.voting_system.election.entity.ElectionR2dbcEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ElectionR2dbcRepository: CoroutineCrudRepository<ElectionR2dbcEntity, String> {
    suspend fun deleteByLastModifiedBefore(pointInTime: LocalDateTime): Long

    @Query("select is_open from election where id = :id")
    suspend fun getIsOpenBy(id: String): Boolean?
}