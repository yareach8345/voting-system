package com.yareach._2_voting_system.integration.vote

import com.yareach._2_voting_system.vote.dto.request.VoteStateChangeRequest
import com.yareach._2_voting_system.vote.dto.response.VoteInfoResponse
import com.yareach._2_voting_system.vote.dto.response.VoteStateChangeResponse
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.service.VoteService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@SpringBootTest
@AutoConfigureWebTestClient
class VoteTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var voteService: VoteService

    @Autowired
    private lateinit var voteRepository: VoteRepository

    @Nested
    @DisplayName("투표 조회")
    inner class FindVoteTest {

        @Test
        @DisplayName("모든 투표 조회")
        fun findAllVotesTest() = runTest {
            webTestClient.get()
                .uri("/votes")
                .exchange()
                .expectStatus().isOk
                .expectBodyList<VoteInfoResponse>()
        }

        @Test
        @DisplayName("id를 사용해 특정 투표 조회")
        fun findAVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            webTestClient.get()
                .uri("/votes/$voteId")
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteInfoResponse>()
                .value {
                    assertNotNull(it)
                    assertEquals(voteId, it.id)
                }
        }

        @Test
        @DisplayName("유효하지 않은 id를 사용해 조회")
        fun findAVoteTestWithWrongId() = runTest {
            val wrongUuid = "uuid-not-exists"
            webTestClient.get()
                .uri("/votes/$wrongUuid")
                .exchange()
                .expectStatus().isNotFound
        }
    }

    @Nested
    @DisplayName("투표 삭제")
    inner class DeleteVoteTest {

        @Test
        @DisplayName("투표 id를 사용하여 투표 삭제")
        fun deleteVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            webTestClient.delete()
                .uri("/votes/$voteId")
                .exchange()
                .expectStatus().isOk

            val vote = voteRepository.findById(voteId)
            assertNull(vote)
        }
    }

    @Nested
    @DisplayName("투표 상태 변경")
    inner class ChangeVoteStateTest {

        @Test
        @DisplayName("투표 open")
        fun openVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            val timeBeforeSendRequest = LocalDateTime.now()

            webTestClient.patch()
                .uri("/votes/$voteId/state")
                .bodyValue(VoteStateChangeRequest("open"))
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteStateChangeResponse>()
                .value {
                    assertNotNull(it)
                    assertEquals(voteId, it.voteId)
                    assertEquals("open", it.newState)
                    assert(it.updatedTime.isAfter(timeBeforeSendRequest))
                    assert(it.updatedTime.isBefore(LocalDateTime.now()))
                }
        }

        @Test
        @DisplayName("투표 close")
        fun closeVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            val timeBeforeSendRequest = LocalDateTime.now()

            webTestClient.patch()
                .uri("/votes/$voteId/state")
                .bodyValue(VoteStateChangeRequest("close"))
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteStateChangeResponse>()
                .value {
                    assertNotNull(it)
                    assertEquals(voteId, it.voteId)
                    assertEquals("close", it.newState)
                    assert(it.updatedTime.isAfter(timeBeforeSendRequest))
                    assert(it.updatedTime.isBefore(LocalDateTime.now()))
                }
        }

        @Test
        @DisplayName("잘못된 상태 투표상태 변경 시도")
        fun tryChangeVoteStateWithWrongStateTest() = runTest {
            val voteId = voteService.createNewVote()

            webTestClient.patch()
                .uri("/votes/$voteId/state")
                .bodyValue(VoteStateChangeRequest("wrong state"))
                .exchange()
                .expectStatus().isBadRequest
        }
    }
}