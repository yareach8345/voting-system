package com.yareach._2_voting_system.vote.controller

import com.yareach._2_voting_system.vote.dto.request.VoteStateChangeRequest
import com.yareach._2_voting_system.vote.dto.response.VoteGenerateResponse
import com.yareach._2_voting_system.vote.dto.response.VoteInfoResponse
import com.yareach._2_voting_system.vote.dto.response.VoteStateChangeResponse
import com.yareach._2_voting_system.vote.service.VoteService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URI

@Controller
@RequestMapping("/votes")
class VoteController(
    val voteService: VoteService
) {
    @GetMapping
    suspend fun getAllVotes(): ResponseEntity<List<VoteInfoResponse>> =
        voteService.getAllVotes()
            .map { VoteInfoResponse.fromVote(it) }
            .toList()
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{voteId}")
    suspend fun getVote(
        @PathVariable voteId: String
    ): ResponseEntity<VoteInfoResponse> = voteService.getVote(voteId)
        .let { VoteInfoResponse.fromVote(it) }
        .let { ResponseEntity.ok(it) }

    @PostMapping
    suspend fun generateVote(): ResponseEntity<VoteGenerateResponse> {
        val voteId = voteService.createNewVote()
        return ResponseEntity
            .created(URI("/vote/${voteId}"))
            .body(VoteGenerateResponse(voteId))
    }

    @DeleteMapping("/{voteId}")
    suspend fun deleteVote(
        @PathVariable voteId: String
    ): ResponseEntity<Unit> {
        voteService.deleteVote(voteId)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{voteId}/state")
    suspend fun openVote(
        @PathVariable voteId: String,
        @RequestBody changeStateRequest: VoteStateChangeRequest
    ): ResponseEntity<VoteStateChangeResponse> {
        val result = voteService.changeVoteState(voteId, changeStateRequest.newState)
        val newState = if(result.isOpen) "open" else "close"
        val updatedTime = if(result.isOpen) result.startedAt else result.endedAt

        return ResponseEntity.ok(VoteStateChangeResponse(voteId, newState, updatedTime ?: throw Error("State Error")))
    }
}