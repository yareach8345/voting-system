package com.yareach._2_voting_system.vote.controller

import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.service.VoteService
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// 테스트를 위해 대충만든 컨트롤러. 개발 후 지울것
@RestController
@RequestMapping("/votes")
class TestVoteController(
    val voteService: VoteService,
) {
    @GetMapping
    suspend fun getVotes(): List<Vote> {
        return voteService.getAllVotes().toList()
    }

    @GetMapping("/{voteId}")
    suspend fun getVote(@PathVariable voteId: String): Vote {
        return voteService.getVote(voteId)
    }

    @PostMapping
    suspend fun addVote(): String {
        return voteService.createNewVote()
    }

    @DeleteMapping
    suspend fun deleteVote(@RequestParam("voteId") voteId: String) {
        voteService.deleteVote(voteId)
    }
}