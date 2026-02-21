package com.yareach.voting_system.integration.election

import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.core.error.ErrorResponseDto
import com.yareach.voting_system.election.dto.ChangeElectionStateRequestDto
import com.yareach.voting_system.election.dto.GenerateElectionResponseDto
import com.yareach.voting_system.election.dto.ElectionInfoResponseDto
import com.yareach.voting_system.election.dto.ChangeElectionStateResponseDto
import com.yareach.voting_system.election.dto.GetNumberOfElectionsResponseDto
import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionRepository
import com.yareach.voting_system.election.scheduler.ElectionExpireScheduler
import com.yareach.voting_system.election.service.ElectionService
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertInstanceOf
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
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.QueryParametersSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.restdocs.snippet.Attributes.key
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.forEach
import kotlin.random.Random
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

    private val identifierBase = "elections"
    private val genIdentifier = { method: String ->
        { case: String -> "$identifierBase/$method/$case" }
    }

    fun assertErrorResponse(errorCode: ErrorCode, errorResponse: ErrorResponseDto?) {
        org.junit.jupiter.api.assertNotNull(errorResponse)
        assertEquals(errorCode.state, errorResponse.state)
        assertEquals(errorCode.errorCode, errorResponse.errorCode)
        assertEquals(errorCode.message, errorResponse.message)
    }

    private val errorResponseFieldsSnippet: ResponseFieldsSnippet = responseFields(
        fieldWithPath("state").description("http state"),
        fieldWithPath("errorCode").description("errorCode"),
        fieldWithPath("message").description("에러 메시지"),
        fieldWithPath("detail").description("상세 설명"),
    )

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

    @BeforeEach
    suspend fun resetDatabase() {
        electionRepository.deleteAll()
    }

    @Nested
    @DisplayName("투표 생성")
    inner class CreateElectionTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("create")

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
                    genIdentifier("success"),
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
    @DisplayName("모든 투표 조회")
    inner class FindAllElectionTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("find-all")

        @Test
        @DisplayName("모든 투표 조회")
        fun findAllElectionsTest() = runTest {
            val size = 6

            repeat(size) {
                val id = electionService.createNewElection()
                val flag = Random.nextInt(3)
                if(flag == 1) {
                    electionService.openElection(id)
                } else if (flag == 2) {
                    electionService.openElection(id)
                    electionService.closeElection(id)
                }
            }

            webTestClient.get()
                .uri("/elections")
                .exchange()
                .expectStatus().isOk
                .expectBody<Collection<ElectionInfoResponseDto>>()
                .value {
                    assertNotNull(it)
                    it.forEach { election -> assertInstanceOf<ElectionInfoResponseDto>(election) }
                }
                .consumeWith(
                    document(
                        genIdentifier("success"),
                        responseFields(
                            fieldWithPath("[].id").description("투표 식별자"),
                            fieldWithPath("[].state").description("투표의 진행 여부"),
                            fieldWithPath("[].startedAt").description("투표 시작 시간").optional(),
                            fieldWithPath("[].endedAt").description("투표 종료 시간").optional(),
                            fieldWithPath("[].createdAt").description("투표 생성 시간").type("String"),
                        )
                    )
                )
        }
    }

    @Nested
    @DisplayName("투표 개수 조회")
    inner class GetNumberOfElectionsTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("get-number-of-elections")

        @Test
        @DisplayName("[성공 케이스] 투표 개수 반환")
        fun findAllElectionsTest() = runTest {
            val size = Random.nextLong(10)
            repeat(size.toInt()) {
                electionService.createNewElection()
            }

            webTestClient.get()
                .uri("/elections/count")
                .exchange()
                .expectStatus().isOk
                .expectBody<GetNumberOfElectionsResponseDto>()
                .value {
                    assertNotNull(it)
                    assertEquals(size, it.count)
                }.consumeWith(document(
                    genIdentifier("success"),
                    responseFields(
                        fieldWithPath("count").description("투표의 개수"),
                        fieldWithPath("aggregatedAt").description("개수를 얻어온 시간")
                    )
                ))
        }
    }

    @Nested
    @DisplayName("페이징을 사용한 데이터 조회 테스트")
    inner class FindWithPagingTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("find-with-paging")

        private val querySnippet: QueryParametersSnippet = queryParameters(
            parameterWithName("page").description("현재 페이지"),
            parameterWithName("size").description("한 페이지에 포함될 데이터 수")
        )

        private lateinit var electionIds: List<String>

        @BeforeEach
        suspend fun prepareElections() {
            electionIds = List(10) {
                val id = electionService.createNewElection()
                val flag = Random.nextInt(3)
                if(flag == 1) {
                    electionService.openElection(id)
                } else if (flag == 2) {
                    electionService.openElection(id)
                    electionService.closeElection(id)
                }
                id
            }
        }

        @Test
        @DisplayName("[성공 케이스] 페이징을 사용해 원하는 페이지의 원하는 개수만큼 조회")
        fun findAllTest() = runTest {
            webTestClient.get()
                .uri("/elections?page=2&size=3")
                .exchange()
                .expectStatus().isOk
                .expectBody<Collection<ElectionInfoResponseDto>>()
                .value {
                    assertNotNull(it)
                    it.forEach { election -> assertInstanceOf<ElectionInfoResponseDto>(election) }
                    it.zip(electionIds.slice(3 until 6)).forEach { (result, testId) -> assertEquals(testId, result.id) }
                }
                .consumeWith(
                    document(
                        genIdentifier("success"),
                        querySnippet,
                        responseFields(
                            fieldWithPath("[].id").description("투표 식별자"),
                            fieldWithPath("[].state").description("투표의 진행 여부"),
                            fieldWithPath("[].startedAt").description("투표 시작 시간").optional(),
                            fieldWithPath("[].endedAt").description("투표 종료 시간").optional(),
                            fieldWithPath("[].createdAt").description("투표 생성 시간").type("String"),
                        )
                    )
                )
        }

        @Test
        @DisplayName("[성공 케이스] 페이지가 넘어갈 경우 빈 배열을 받음")
        fun pageOverflowTest() = runTest {
            webTestClient.get()
                .uri("/elections?page=3&size=6")
                .exchange()
                .expectStatus().isOk
                .expectBody<Collection<ElectionInfoResponseDto>>()
                .value {
                    assertNotNull(it)
                    assertEquals(0, it.size)
                }
                .consumeWith(
                    document(
                        genIdentifier("success-page-overflow"),
                        querySnippet,
                        responseFields(
                            fieldWithPath("[]").description("빈 배열")
                        )
                    )
                )
        }

        @Test
        @DisplayName("[실패 케이스] 페이지 수가 1미만일 경우 실패")
        fun pageIsLessThenOne() = runTest {
            webTestClient.get()
                .uri("/elections?page=-1&size=6")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.PAGING_ERROR, it) }
                .consumeWith(
                    document(
                        genIdentifier("paging-error-page-less-then-1"),
                        querySnippet,
                        errorResponseFieldsSnippet
                    )
                )
        }

        @Test
        @DisplayName("[실패 케이스] size가 1미만일 경우 실패")
        fun sizeIsLessThenOne() = runTest {
            webTestClient.get()
                .uri("/elections?page=2&size=0")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.PAGING_ERROR, it) }
                .consumeWith(
                    document(
                        genIdentifier("paging-error-size-less-then-1"),
                        querySnippet,
                        errorResponseFieldsSnippet
                    )
                )
        }
    }

    @Nested
    @DisplayName("id로 투표 조회")
    inner class FindElectionByIdTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("find-by-id")

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
                    genIdentifier("success"),
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
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
                .consumeWith(
                    document(
                        genIdentifier("election-is-not-found"),
                        pathParameters(parameterWithName("electionId").description("투표 식별자")),
                        errorResponseFieldsSnippet
                ))
        }
    }

    @Nested
    @DisplayName("투표 삭제")
    inner class DeleteElectionTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("delete")

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
                    genIdentifier("success"),
                    pathParameters(parameterWithName("electionId").description("투표 식별자"))
                ))

            val election = electionRepository.findById(electionId)
            assertNull(election)
        }

        @Test
        @DisplayName("id에 해당하는 투표가 존재하지 않음")
        fun electionIdNotExists() = runTest {
            val unexistsElectionId = UUID.randomUUID().toString()

            webTestClient.delete()
                .uri("/elections/{electionId}", unexistsElectionId)
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
                .consumeWith(document(
                    genIdentifier("election-not-found"),
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                    errorResponseFieldsSnippet
                ))
        }
    }

    @Nested
    @DisplayName("투표 상태 변경")
    inner class ChangeElectionStateTest {

        private val genIdentifier = this@ElectionTest.genIdentifier("change-state")

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
                    genIdentifier("success-open"),
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
                    genIdentifier("success-close"),
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
                .value { assertErrorResponse(ErrorCode.VALIDATION_FAILED, it) }
                .consumeWith(document(
                    genIdentifier("invalid-election-state"),
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                    errorResponseFieldsSnippet
                ))
        }

        @Test
        @DisplayName("투표가 존재하지 않음")
        fun electionIsNotExists() = runTest {
            val unexistElectionId = UUID.randomUUID().toString()

            webTestClient.patch()
                .uri("/elections/{electionId}/state", unexistElectionId)
                .bodyValue(ChangeElectionStateRequestDto("open"))
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponseDto>()
                .value { assertErrorResponse(ErrorCode.ELECTION_NOT_FOUND, it) }
                .consumeWith(document(
                    genIdentifier("election-not-exists"),
                    pathParameters(parameterWithName("electionId").description("투표 식별자")),
                    errorResponseFieldsSnippet
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

            val cutoff = LocalDateTime.now().minusSeconds(10)

            val numberOfElectionsBeforeExpire = electionRepository.findAll().count()

            electionService.deleteExpiredElections(cutoff)

            val numberOfElectionsAfterExpire = electionRepository.findAll().count()

            assertEquals(2, numberOfElectionsBeforeExpire - numberOfElectionsAfterExpire)
        }
    }
}