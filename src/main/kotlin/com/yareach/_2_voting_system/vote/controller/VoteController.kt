package com.yareach._2_voting_system.vote.controller

import com.yareach._2_voting_system.vote.dto.ChangeVoteStateRequestDto
import com.yareach._2_voting_system.vote.dto.GenerateVoteResponseDto
import com.yareach._2_voting_system.vote.dto.VoteInfoResponseDto
import com.yareach._2_voting_system.vote.dto.ChangeVoteStateResponseDto
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
    suspend fun getAllVotes(): ResponseEntity<List<VoteInfoResponseDto>> =
        voteService.getAllVotes()
            .map { VoteInfoResponseDto.fromVote(it) }
            .toList()
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{voteId}")
    suspend fun getVote(
        @PathVariable voteId: String
    ): ResponseEntity<VoteInfoResponseDto> = voteService.getVote(voteId)
        .let { VoteInfoResponseDto.fromVote(it) }
        .let { ResponseEntity.ok(it) }

    @PostMapping
    suspend fun generateVote(): ResponseEntity<GenerateVoteResponseDto> {
        val voteId = voteService.createNewVote()
        return ResponseEntity
            .created(URI("/votes/${voteId}"))
            .body(GenerateVoteResponseDto(voteId))
    }

    @DeleteMapping("/{voteId}")
    suspend fun deleteVote(
        @PathVariable voteId: String
    ): ResponseEntity<Unit> {
        voteService.deleteVote(voteId)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{voteId}/state")
    suspend fun changeVoteState(
        @PathVariable voteId: String,
        @RequestBody changeStateRequest: ChangeVoteStateRequestDto
    ): ResponseEntity<ChangeVoteStateResponseDto> {
        val result = voteService.changeVoteState(voteId, changeStateRequest.newState)

        return ResponseEntity.ok(ChangeVoteStateResponseDto.fromNewVoteModel(result))
    }
}