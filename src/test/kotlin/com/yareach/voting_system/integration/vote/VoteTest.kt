package com.yareach.voting_system.integration.vote

import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.core.error.ErrorResponseDto
import com.yareach.voting_system.election.service.ElectionService
import com.yareach.voting_system.vote.dto.RecordVoteRequestDto
import com.yareach.voting_system.vote.dto.UpdateVoteItemRequestDto
import com.yareach.voting_system.vote.dto.VoteInfoResponseDto
import com.yareach.voting_system.vote.dto.VoteStatisticsResponseDto
import com.yareach.voting_system.vote.service.VoteService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest( properties = [
    "vote.validation.item.use-validator=true",
    "vote.validation.item.regex-string=[0-9]{2}-[0-9]{2}",
    "vote.validation.user-id.use-validator=true",
    "vote.validation.user-id.regex-string=(user|admin)-[0-9]+",
])
@AutoConfigureWebTestClient
@ExtendWith(RestDocumentationExtension::class)
class VoteTest(@Autowired private val voteService: VoteService) {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var electionService: ElectionService

    fun assertErrorResponse(errorCode: ErrorCode, errorResponse: ErrorResponseDto?) {
        assertNotNull(errorResponse)
        assertEquals(errorCode.state, errorResponse.state)
        assertEquals(errorCode.errorCode, errorResponse.errorCode)
        assertEquals(errorCode.message, errorResponse.message)
    }

    @BeforeEach
    fun setUp(context: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
            .configureClient()
            .filter(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .build()
    }

    @Nested
    @DisplayName("Record Vote Test")
    inner class RecordVoteTest {

        @Test
        @DisplayName("[성공 케이스] 데이터베이스에 투표 기록이 등록됨")
        fun voteBeRecordedSuccessfully() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val requestBody = RecordVoteRequestDto(
                userId = "user-01",
                item = "12-34"
            )

            webTestClient.post()
                .uri("/elections/{electionId}/votes", electionId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().valueMatches("Location", "/elections/${electionId}/votes/${requestBody.userId}")
                .expectBody<VoteInfoResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals(requestBody.userId, it.userId)
                    assertEquals(requestBody.item, it.item)
                }
        }

        @Test
        @DisplayName("[실패 케이스] 존재하지 않는 election으로 투표시도시 실패")
        fun failRecordVoteCuzElectionIsNotExists() = runTest {
            val wrongElectionId = "wrongElectionId"

            val requestBody = RecordVoteRequestDto(
                userId = "user-01",
                item = "12-34"
            )

            webTestClient.post()
                .uri("/elections/{electionId}/votes", wrongElectionId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 잘못된 Item에 투표하려하여 실패")
        fun failRecordVoteCuzItemIsWrong() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val requestBody = RecordVoteRequestDto(
                userId = "user-01",
                item = "12-asd"
            )

            webTestClient.post()
                .uri("/elections/{electionId}/votes", electionId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.NOT_VALID_ITEM, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 잘못된 UserId로 실패")
        fun failRecordVoteCuzUserIdIsWrong() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val requestBody = RecordVoteRequestDto(
                userId = "tester-01",
                item = "12-34"
            )

            webTestClient.post()
                .uri("/elections/{electionId}/votes", electionId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.NOT_VALID_USERID, it) }
        }
    }

    @Nested
    @DisplayName("Find Vote Test")
    inner class FindVoteTest {

        @Test
        @DisplayName("[성공 케이스] 투표id와 유저의 id로 투표기록 조회")
        fun findVoteWithElectionIdAndUserId() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val userId = "user-01"

            val item = "12-34"

            voteService.record(electionId, userId, item)

            webTestClient.get()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteInfoResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals(userId, it.userId)
                    assertEquals(item, it.item)
                }
        }

        @Test
        @DisplayName("[성공 케이스] 투표가 종료된 후에도 불러올 수 있음")
        fun findVoteWithElectionIdAndUserIdAfterCloseElection() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val userId = "user-01"

            val item = "12-34"

            voteService.record(electionId, userId, item)

            electionService.closeElection(electionId)

            webTestClient.get()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteInfoResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals(userId, it.userId)
                    assertEquals(item, it.item)
                }
        }

        @Test
        @DisplayName("[실패 케이스] 해당하는 투표가 존재하지 않아 실패")
        fun failCuzVoteIsNotExists() = runTest {
            val electionId = UUID.randomUUID()

            val unexistUserId = "user-12"

            webTestClient.get()
                .uri("/elections/{electionId}/votes/{userId}", electionId, unexistUserId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.VOTE_NOT_FOUND, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 유저id에 해당하는 투표가 존재하지 않아 실패")
        fun failCuzVoteIsNotExistsForUserId() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val unexistUserId = "user-12"

            webTestClient.get()
                .uri("/elections/{electionId}/votes/{userId}", electionId, unexistUserId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.VOTE_NOT_FOUND, it) }
        }
    }


    @Nested
    @DisplayName("Get Vote Statistics Test")
    inner class GetVoteStatisticsTest {

        @Test
        @DisplayName("[성공 케이스] Election의 현재 투표 현황을 불러옴")
        fun getStatistics() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val size = 20
            val userIds = (0 until size).map { "user-${it}" }
            val items = (0 until size).map {
                val num1 = Random.nextInt(0, 3)
                val num2 = Random.nextInt(0, 3)
                "%02d-%02d".format(num1, num2)
            }

            val itemCountMap = items.groupBy { it }.mapValues { it.value.size.toLong() }

            userIds.zip(items).forEach { (userId, item) ->
                voteService.record(electionId, userId, item)
            }

            webTestClient.get()
                .uri("/elections/{electionId}/votes/statistic", electionId)
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteStatisticsResponseDto>()
                .value {
                    assertNotNull(it)
                    it.voteCounts.forEach{ voteCount -> assertEquals(itemCountMap[voteCount.item], voteCount.voteCount) }
                }
        }

        @Test
        @DisplayName("[실패 케이스] Election이 존재하지 않음")
        fun tryGetStatisticsWithWrongElectionId() = runTest {
            val wrongElectionId = UUID.randomUUID().toString()

            webTestClient.get()
                .uri("/elections/{electionId}/votes/statistic", wrongElectionId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value {
                    assertNotNull(it)
                    assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it)
                }
        }
    }

    @Nested
    @DisplayName("Delete Votes with ElectionId Test")
    inner class DeleteVotesByElectionIdTest{

        @Test
        @DisplayName("[성공 케이스] 투표 id로 투표의 모든 기록을 삭제")
        fun successDeleteVotesByElectionId() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            (0 until 10).forEach { voteService.record(electionId, "user-$it", "0$it-0$it") }

            webTestClient.delete()
                .uri("/elections/{electionId}", electionId)
                .exchange()
                .expectStatus().isOk
        }

        @Test
        @DisplayName("[실패 케이스] 투표 id에 해당하는 투표가 존재하지 않음")
        fun failDeleteVotesCuzElectionIsNotFound() = runTest {
            val unexistsElectionId = UUID.randomUUID().toString()

            webTestClient.delete()
                .uri("/elections/{electionId}/votes", unexistsElectionId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
        }
    }

    @Nested
    @DisplayName("Cancel Vote Test")
    inner class CancelVoteTest {

        @Test
        @DisplayName("[성공 케이스] 처리가 완료 됨")
        fun cancelVoteSuccessfully() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val userId = "user-01"

            val item = "12-34"

            voteService.record(electionId, userId, item)

            webTestClient.delete()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .exchange()
                .expectStatus().isOk
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 열려있지 않은 경우 실패함")
        fun failCancelVoteElectionIsClose() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val userId = "user-01"

            val item = "12-34"

            voteService.record(electionId, userId, item)

            electionService.closeElection(electionId)

            webTestClient.delete()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_IS_NOT_OPEN, it) }
        }
    }

    @Nested
    @DisplayName("Change Item Test")
    inner class ChangeItemTest {

        @Test
        @DisplayName("[성공 케이스] 투표가 업데이트 됨")
        fun changeItemSuccessfully() = runTest {
            val electionId = electionService.createNewElection()
            electionService.openElection(electionId)

            val userId = "user-01"

            val item = "12-34"

            val newItem = "99-99"

            val voteBeforeUpdate = voteService.record(electionId, userId, item)

            val requestBody = UpdateVoteItemRequestDto(newItem)

            webTestClient.patch()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteInfoResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals(userId, it.userId)
                    assertEquals(newItem, it.item)
                    assert(it.votedAt.isAfter(voteBeforeUpdate.votedAt))
                }
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 열려있지 않음")
        fun failChangeItemCuzElectionIsNotOpen() = runTest {
            val electionId = electionService.createNewElection()

            val userId = "user-01"

            val newItem = "12-34"

            val requestBody = UpdateVoteItemRequestDto(newItem)

            webTestClient.patch()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_IS_NOT_OPEN, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 존재하지 않음")
        fun failChangeItemCuzElectionIsNotFound() = runTest {
            val electionId = UUID.randomUUID().toString()

            val userId = "user-01"

            val newItem = "12-34"

            val requestBody = UpdateVoteItemRequestDto(newItem)

            webTestClient.patch()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 유효하지 않은 UserId")
        fun failChangeItemCuzIllegalUserId() = runTest {
            val electionId = UUID.randomUUID().toString()

            val userId = "sung-the-tester"

            val newItem = "12-34"

            val requestBody = UpdateVoteItemRequestDto(newItem)

            webTestClient.patch()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.NOT_VALID_USERID, it) }
        }

        @Test
        @DisplayName("[실패 케이스] 유효하지 않은 item")
        fun failChangeItemCuzIllegalItem() = runTest {
            val electionId = UUID.randomUUID().toString()

            val userId = "user-01"

            val newItem = "132-32"

            val requestBody = UpdateVoteItemRequestDto(newItem)

            webTestClient.patch()
                .uri("/elections/{electionId}/votes/{userId}", electionId, userId)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.NOT_VALID_ITEM, it) }
        }
    }
}