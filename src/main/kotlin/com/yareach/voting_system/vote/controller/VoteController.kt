package com.yareach.voting_system.vote.controller

import com.yareach.voting_system.vote.dto.RecordVoteRequestDto
import com.yareach.voting_system.vote.dto.UpdateVoteItemRequestDto
import com.yareach.voting_system.vote.dto.VoteInfoResponseDto
import com.yareach.voting_system.vote.dto.VoteStatisticsResponseDto
import com.yareach.voting_system.vote.service.VoteService
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDateTime

@RestController
@RequestMapping("elections/{electionId}/votes")
class VoteController(
    private val voteService: VoteService,
) {

    @PostMapping
    suspend fun recordVote(
        @PathVariable electionId: String,
        @RequestBody recordVoteRequest: RecordVoteRequestDto,
    ): ResponseEntity<VoteInfoResponseDto> {
        val userId = recordVoteRequest.userId
        val item = recordVoteRequest.item

        val vote = voteService.record( electionId, userId, item )

        val location = URI("/elections/${vote.electionId}/votes/${vote.userId}")

        return ResponseEntity
            .created(location)
            .body(VoteInfoResponseDto.fromVote(vote))
    }

    @GetMapping("/{userId}")
    suspend fun getVote(
        @PathVariable electionId: String,
        @PathVariable userId: String,
    ): ResponseEntity<VoteInfoResponseDto> {
        val responseBody = voteService.getVoteInfo(electionId, userId)
            .let { VoteInfoResponseDto.fromVote(it) }

        return ResponseEntity.ok(responseBody)
    }

    @GetMapping("/statistic")
    suspend fun getVoteStatistics(
        @PathVariable electionId: String
    ): ResponseEntity<VoteStatisticsResponseDto> {
        val statistics = voteService.getElectionStatistics(electionId)
        val responseBody = VoteStatisticsResponseDto.from(statistics.toList(), LocalDateTime.now())

        return ResponseEntity.ok(responseBody)
    }

    @DeleteMapping
    suspend fun deleteVotes(
        @PathVariable electionId: String
    ): ResponseEntity<Unit> {
        voteService.deleteByElectionId(electionId)

        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{userId}")
    suspend fun cancelVote(
        @PathVariable electionId: String,
        @PathVariable userId: String
    ) : ResponseEntity<Unit> {
        voteService.cancel(electionId, userId)

        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{userId}")
    suspend fun changeItem(
        @PathVariable electionId: String,
        @PathVariable userId: String,
        @RequestBody updateVoteItemRequest: UpdateVoteItemRequestDto,
    ): ResponseEntity<VoteInfoResponseDto> {
        val newItem = updateVoteItemRequest.item

        val updatedVote = voteService.changeItem( electionId, userId, newItem )

        val responseDto = VoteInfoResponseDto.fromVote(updatedVote)

        return ResponseEntity.ok(responseDto)
    }
}