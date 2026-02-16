package com.yareach._2_voting_system.integration.vote

import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.error.ErrorResponseDto
import com.yareach._2_voting_system.election.dto.ChangeElectionStateRequestDto
import com.yareach._2_voting_system.election.dto.GenerateElectionResponseDto
import com.yareach._2_voting_system.election.dto.ElectionInfoResponseDto
import com.yareach._2_voting_system.election.dto.ChangeElectionStateResponseDto
import com.yareach._2_voting_system.election.model.Election
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.election.scheduler.ElectionExpireScheduler
import com.yareach._2_voting_system.election.service.ElectionService
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
import org.springframework.http.MediaType
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
class ElectionTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var electionService: ElectionService

    @Autowired
    private lateinit var electionRepository: ElectionRepository
    
    @Autowired
    private lateinit var expireScheduler: ElectionExpireScheduler

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
    inner class CreateElectionTest {

        @Test
        @DisplayName("새로운 투표 생성")
        fun createElection() {
            webTestClient.post()
                .uri("/elections")
                .exchange()
                .expectStatus().isCreated
                .expectHeader().valueMatches("Location", "^/elections/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")
                .expectBody<GenerateElectionResponseDto>()
                .value {
                    assertNotNull(it)
                    assertDoesNotThrow{ UUID.fromString(it.newElectionId) }
                }
                .consumeWith(document(
                    "create-election",
                    responseFields(
                        fieldWithPath("newElectionId").description("생성된 투표의 id").attributes(key("format").value("uuid"))
                    ),
                    responseHeaders(
                        headerWithName("Location").description("새로 생성된 투표 uri"),
                    )
            ))
        }
    }

    @Nested
    @DisplayName("투표 조회")
    inner class FindElectionTest {

        @Test
        @DisplayName("모든 투표 조회")
        fun findAllElectionsTest() = runTest {
            webTestClient.get()
                .uri("/elections")
                .exchange()
                .expectStatus().isOk
                .expectBody<Collection<ElectionInfoResponseDto>>()
                .consumeWith(document(
                    "get-all-elections",
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
        fun findElectionTest() = runTest {
            val electionId = electionService.createNewElection()

            webTestClient.get()
                .uri("/elections/{electionId}", electionId)
                .exchange()
                .expectStatus().isOk
                .expectBody<ElectionInfoResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.id)
                }.consumeWith(document(
                    "get-a-election-with-id",
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
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
        fun findElectionTestWithWrongId() = runTest {
            val wrongElectionId = "uuid-not-exists"
            webTestClient.get()
                .uri("/elections/{electionId}", wrongElectionId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .consumeWith(
                    document(
                        "get-a-election-with-wrong-id",
                        pathParameters(parameterWithName("electionId").description("투표 식별자")),
                ))
        }
    }

    @Nested
    @DisplayName("투표 삭제")
    inner class DeleteElectionTest {

        @Test
        @DisplayName("투표 id를 사용하여 투표 삭제")
        fun deleteElectionTest() = runTest {
            val electionId = electionService.createNewElection()

            webTestClient.delete()
                .uri("/elections/{electionId}", electionId)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith(document(
                    "delete-electionId",
                    pathParameters(parameterWithName("electionId").description("투표 식별자"))
                ))

            val election = electionRepository.findById(electionId)
            assertNull(election)
        }
    }

    @Nested
    @DisplayName("투표 상태 변경")
    inner class ChangeElectionStateTest {

        @Test
        @DisplayName("투표 open")
        fun openElectionTest() = runTest {
            val electionId = electionService.createNewElection()

            val timeBeforeSendRequest = LocalDateTime.now()

            webTestClient.patch()
                .uri("/elections/{electionId}/state", electionId)
                .bodyValue(ChangeElectionStateRequestDto("open"))
                .exchange()
                .expectStatus().isOk
                .expectBody<ChangeElectionStateResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals("open", it.newState)
                    assert(it.updatedTime.isAfter(timeBeforeSendRequest))
                    assert(it.updatedTime.isBefore(LocalDateTime.now()))
                }.consumeWith(document(
                    "open-election",
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                    requestFields(fieldWithPath("newState").description("변경할 상태")),
                    responseFields(
                        fieldWithPath("electionId").description("변경된 투표의 식별자"),
                        fieldWithPath("newState").description("변경된 새로운 상태"),
                        fieldWithPath("updatedTime").description("상태가 변경된 시각"),
                    ),
                ))
        }

        @Test
        @DisplayName("투표 close")
        fun closeElectionTest() = runTest {
            val electionId = electionService.createNewElection()

            val timeBeforeSendRequest = LocalDateTime.now()

            webTestClient.patch()
                .uri("/elections/{electionId}/state", electionId)
                .bodyValue(ChangeElectionStateRequestDto("close"))
                .exchange()
                .expectStatus().isOk
                .expectBody<ChangeElectionStateResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(electionId, it.electionId)
                    assertEquals("close", it.newState)
                    assert(it.updatedTime.isAfter(timeBeforeSendRequest))
                    assert(it.updatedTime.isBefore(LocalDateTime.now()))
                }.consumeWith(document(
                    "close-electionId",
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                    requestFields(fieldWithPath("newState").description("변경할 상태")),
                    responseFields(
                        fieldWithPath("electionId").description("변경된 투표의 식별자"),
                        fieldWithPath("newState").description("변경된 새로운 상태"),
                        fieldWithPath("updatedTime").description("상태가 변경된 시각"),
                    ),
                ))
        }

        @Test
        @DisplayName("잘못된 상태 투표상태 변경 시도")
        fun tryChangeElectionStateWithWrongStateTest() = runTest {
            val electionId = electionService.createNewElection()

            webTestClient.patch()
                .uri("/elections/{electionId}/state", electionId)
                .bodyValue(ChangeElectionStateRequestDto("wrong state"))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(ErrorCode.VALIDATION_FAILED.state, it.state)
                    assertEquals(ErrorCode.VALIDATION_FAILED.errorCode, it.errorCode)
                }
                .consumeWith(document(
                    "change-election-state-fail",
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                ))
        }
    }

    @Nested
    @DisplayName("투표 만료")
    inner class ExpireElectionTest {

        @Test
        @DisplayName("만료시간이 지날 시 삭제됨")
        fun expireElectionTest() = runTest {
            val numberOfWillExpiredElections = 2

            List(numberOfWillExpiredElections) { Election.new().apply { lastModified = LocalDateTime.now().minusSeconds(11) } }
                .forEach{ electionRepository.insert(it) }

            val numberOfElectionsBeforeExpire = electionRepository.findAll().count()

            electionService.deleteExpiredElections()

            val numberOfElectionsAfterExpire = electionRepository.findAll().count()

            assertEquals(2, numberOfElectionsBeforeExpire - numberOfElectionsAfterExpire)
        }
    }
}