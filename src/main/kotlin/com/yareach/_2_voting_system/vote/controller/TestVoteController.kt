package com.yareach._2_voting_system.vote.controller

import com.yareach._2_voting_system.vote.entity.VoteEntity
import com.yareach._2_voting_system.vote.entity.VoteRecordEntity
import com.yareach._2_voting_system.vote.repository.VoteRecordRepository
import com.yareach._2_voting_system.vote.repository.VoteRepository
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// 테스트를 위해 대충만든 컨트롤러. 개발 후 지울것
@RestController
@RequestMapping("/votes")
class TestVoteController(
    val repository: VoteRepository,
    val repository2: VoteRecordRepository
) {
    @GetMapping
    suspend fun getVotes(): List<VoteEntity> {
        return repository.findAll().toList()
    }

    @GetMapping("/{voteId}")
    suspend fun getVote(@PathVariable voteId: String): VoteEntity {
        return repository.findById(voteId) ?: throw Error("Not Found")
    }

    @GetMapping("/voting/records")
    suspend fun getRecords(
        @RequestParam("voteId") voteId: String
    ): List<VoteRecordEntity> {
        return repository2.findAllByVoteId(voteId).toList()
    }
}