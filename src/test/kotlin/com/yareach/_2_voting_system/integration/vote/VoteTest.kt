package com.yareach._2_voting_system.integration.vote

import com.yareach._2_voting_system.vote.dto.request.VoteStateChangeRequest
import com.yareach._2_voting_system.vote.dto.response.VoteGenerateResponse
import com.yareach._2_voting_system.vote.dto.response.VoteInfoResponse
import com.yareach._2_voting_system.vote.dto.response.VoteStateChangeResponse
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.scheduler.VoteExpireScheduler
import com.yareach._2_voting_system.vote.service.VoteService
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.snippet.Attributes.key
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@SpringBootTest(properties = [
    "vote.expire.ttl-seconds=10",
    "vote.expire.delay-sec=2"
])
@AutoConfigureWebTestClient
@ExtendWith(RestDocumentationExtension::class)
class VoteTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var voteService: VoteService

    @Autowired
    private lateinit var voteRepository: VoteRepository
    
    @Autowired
    private lateinit var expireScheduler: VoteExpireScheduler

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
    @DisplayName("투표 생성")
    inner class CreateVoteTest {

        @Test
        @DisplayName("새로운 투표 생성")
        fun createVote() {
            webTestClient.post()
                .uri("/votes")
                .exchange()
                .expectStatus().isCreated
                .expectHeader().valueMatches("Location", "^/votes/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")
                .expectBody<VoteGenerateResponse>()
                .value {
                    assertNotNull(it)
                    assertDoesNotThrow{ UUID.fromString(it.voteId) }
                }
                .consumeWith(document(
                    "create-vote",
                    responseFields(
                        fieldWithPath("voteId").description("생성된 투표의 id").attributes(key("format").value("uuid"))
                    ),
                    responseHeaders(
                        headerWithName("Location").description("새로 생성된 투표 uri"),
                    )
            ))
        }
    }

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
                .expectBody<Collection<VoteInfoResponse>>()
                .consumeWith(document(
                    "get-all-votes",
                    responseFields(
                        fieldWithPath("[].id").description("투표 식별자"),
                        fieldWithPath("[].state").description("투표의 진행 여부"),
                        fieldWithPath("[].startedAt").description("투표 시작 시간").optional(),
                        fieldWithPath("[].endedAt").description("투표 종료 시간").optional(),
                        fieldWithPath("[].createdAt").description("투표 생성 시간").type("String"),
                    )
                ))
        }

        @Test
        @DisplayName("id를 사용해 특정 투표 조회")
        fun findAVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            webTestClient.get()
                .uri("/votes/{voteId}", voteId)
                .exchange()
                .expectStatus().isOk
                .expectBody<VoteInfoResponse>()
                .value {
                    assertNotNull(it)
                    assertEquals(voteId, it.id)
                }.consumeWith(document(
                    "get-a-vote-with-id",
                    pathParameters(parameterWithName("voteId").description("투표 식별자")),
                    responseFields(
                        fieldWithPath("id").description("투표 식별자"),
                        fieldWithPath("state").description("투표의 진행 여부"),
                        fieldWithPath("startedAt").description("투표 시작 시간").optional(),
                        fieldWithPath("endedAt").description("투표 종료 시간").optional(),
                        fieldWithPath("createdAt").description("투표 생성 시간"),
                    )
                ))
        }

        @Test
        @DisplayName("유효하지 않은 id를 사용해 조회")
        fun findAVoteTestWithWrongId() = runTest {
            val wrongUuid = "uuid-not-exists"
            webTestClient.get()
                .uri("/votes/{voteId}", wrongUuid)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .consumeWith(
                    document(
                        "get-a-vote-with-wrong-id",
                        pathParameters(parameterWithName("voteId").description("투표 식별자")),
                ))
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
                .uri("/votes/{voteId}", voteId)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith(document(
                    "delete-vote",
                    pathParameters(parameterWithName("voteId").description("투표 식별자"))
                ))

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
                .uri("/votes/{voteId}/state", voteId)
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
                }.consumeWith(document(
                    "open-vote",
                    pathParameters(parameterWithName("voteId").description("투표 식별자")),
                    requestFields(fieldWithPath("newState").description("변경할 상태")),
                    responseFields(
                        fieldWithPath("voteId").description("변경된 투표의 식별자"),
                        fieldWithPath("newState").description("변경된 새로운 상태"),
                        fieldWithPath("updatedTime").description("상태가 변경된 시각"),
                    ),
                ))
        }

        @Test
        @DisplayName("투표 close")
        fun closeVoteTest() = runTest {
            val voteId = voteService.createNewVote()

            val timeBeforeSendRequest = LocalDateTime.now()

            webTestClient.patch()
                .uri("/votes/{voteId}/state", voteId)
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
                }.consumeWith(document(
                    "close-vote",
                    pathParameters(parameterWithName("voteId").description("투표 식별자")),
                    requestFields(fieldWithPath("newState").description("변경할 상태")),
                    responseFields(
                        fieldWithPath("voteId").description("변경된 투표의 식별자"),
                        fieldWithPath("newState").description("변경된 새로운 상태"),
                        fieldWithPath("updatedTime").description("상태가 변경된 시각"),
                    ),
                ))
        }

        @Test
        @DisplayName("잘못된 상태 투표상태 변경 시도")
        fun tryChangeVoteStateWithWrongStateTest() = runTest {
            val voteId = voteService.createNewVote()

            webTestClient.patch()
                .uri("/votes/{voteId}/state", voteId)
                .bodyValue(VoteStateChangeRequest("wrong state"))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .consumeWith(document(
                    "change-vote-state-fail",
                    pathParameters(parameterWithName("voteId").description("투표 식별자")),
                ))
        }
    }

    @Nested
    @DisplayName("투표 만료")
    inner class ExpireVoteTest {

        @Test
        @DisplayName("만료시간이 지날 시 삭제됨")
        fun expireVoteTest() = runTest {
            val numberOfWillExpiredVotes = 2

            List(numberOfWillExpiredVotes) { Vote.new().apply { lastModified = LocalDateTime.now().minusSeconds(11) } }
                .forEach{ voteRepository.insert(it) }

            val numberOfVotesBeforeExpire = voteRepository.findAll().count()

            voteService.deleteExpiredVotes()

            val numberOfVotesAfterExpire = voteRepository.findAll().count()

            assertEquals(2, numberOfVotesBeforeExpire - numberOfVotesAfterExpire)
        }
    }
}