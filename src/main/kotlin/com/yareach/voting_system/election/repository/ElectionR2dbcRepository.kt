package com.yareach.voting_system.election.repository

import com.yareach.voting_system.election.dto.IsOpenAndCountPairDto
import com.yareach.voting_system.election.entity.ElectionR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ElectionR2dbcRepository: CoroutineCrudRepository<ElectionR2dbcEntity, String> {
    suspend fun deleteByLastModifiedBefore(pointInTime: LocalDateTime): Long

    @Query("select is_open from election where id = :id")
    suspend fun getIsOpenBy(id: String): Boolean?

    @Query("select * from election limit :size offset :size * :page")
    suspend fun findWithPaging(page: Long, size: Long): Flow<ElectionR2dbcEntity>

    @Query("select is_open, count(*) as count from election group by is_open")
    suspend fun countByIsOpen(): Flow<IsOpenAndCountPairDto>
}