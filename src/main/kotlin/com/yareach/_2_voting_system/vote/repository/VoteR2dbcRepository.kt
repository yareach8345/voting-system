package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import com.yareach._2_voting_system.vote.entity.VoteR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteR2dbcRepository: CoroutineCrudRepository<VoteR2dbcEntity, Int> {
    suspend fun findAllByElectionId(electionId: String): Flow<VoteR2dbcEntity>

    suspend fun findByElectionIdAndUserId(electionId: String, userId: String): VoteR2dbcEntity?

    suspend fun deleteAllByElectionId(electionId: String): Long

    suspend fun deleteByElectionIdAndUserId(electionId: String, userId: String): Long

    suspend fun countByElectionId(electionId: String): Long

    @Query("""select item, count(*) as vote_count from vote where election_id = :electionId group by item""")
    suspend fun countGroupByElectionId(electionId: String): Flow<ItemAndVotesCountPairDto>
}