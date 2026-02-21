package com.yareach.voting_system.election.controller

import com.yareach.voting_system.election.dto.ChangeElectionStateRequestDto
import com.yareach.voting_system.election.dto.GenerateElectionResponseDto
import com.yareach.voting_system.election.dto.ElectionInfoResponseDto
import com.yareach.voting_system.election.dto.ChangeElectionStateResponseDto
import com.yareach.voting_system.election.dto.GetNumberOfElectionsResponseDto
import com.yareach.voting_system.election.service.ElectionService
import jakarta.validation.Valid
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
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI

@Controller
@RequestMapping("/elections")
class ElectionController(
    val electionService: ElectionService
) {
    @GetMapping
    suspend fun getAllElections(
        @RequestParam page: Long?,
        @RequestParam size: Long = 10,
    ): ResponseEntity<List<ElectionInfoResponseDto>> {
        val elections = if(page == null) {
            electionService.getAllElections()
        } else {
            electionService.getElectionsWithPage(page - 1, size)
        }

        return elections
            .map { ElectionInfoResponseDto.fromElection(it) }
            .toList()
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{electionId}")
    suspend fun getElection(
        @PathVariable electionId: String
    ): ResponseEntity<ElectionInfoResponseDto> = electionService.getElection(electionId)
        .let { ElectionInfoResponseDto.fromElection(it) }
        .let { ResponseEntity.ok(it) }

    @GetMapping("/count")
    suspend fun getNumberOfElections(): ResponseEntity<GetNumberOfElectionsResponseDto> {
        val numberOfElections = electionService.getNumberOfElections()

        return ResponseEntity.ok(GetNumberOfElectionsResponseDto.fromNumberOfElections(numberOfElections))
    }

    @PostMapping
    suspend fun generateElection(): ResponseEntity<GenerateElectionResponseDto> {
        val newElectionId = electionService.createNewElection()
        return ResponseEntity
            .created(URI("/elections/${newElectionId}"))
            .body(GenerateElectionResponseDto(newElectionId))
    }

    @DeleteMapping("/{electionId}")
    suspend fun deleteElection(
        @PathVariable electionId: String
    ): ResponseEntity<Unit> {
        electionService.deleteElection(electionId)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{electionId}/state")
    suspend fun changeElectionState(
        @PathVariable electionId: String,
        @RequestBody @Valid changeStateRequest: ChangeElectionStateRequestDto
    ): ResponseEntity<ChangeElectionStateResponseDto> {
        val result = electionService.changeElectionState(electionId, changeStateRequest.newState)

        return ResponseEntity.ok(ChangeElectionStateResponseDto.fromNewElectionModel(result))
    }
}