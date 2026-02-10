package com.yareach._2_voting_system.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRepository: CoroutineCrudRepository<VoteEntity, String> { }