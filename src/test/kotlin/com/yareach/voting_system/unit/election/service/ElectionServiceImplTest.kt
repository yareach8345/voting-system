package com.yareach.voting_system.unit.election.service

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.election.dto.IsOpenAndCountPairDto
import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionRepository
import com.yareach.voting_system.election.service.ElectionService
import com.yareach.voting_system.election.service.ElectionServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ElectionServiceImplTest {
    val electionRepositoryMock = mockk<ElectionRepository>()

    val electionService: ElectionService = spyk(ElectionServiceImpl(electionRepositoryMock))

    @Nested
    @DisplayName("새 투표 생성(createNewElection)")
    inner class ElectionCreateTest {

        @Test
        @DisplayName("새 투표 생성, db에 저장")
        fun createNewElectionTest() = runTest {

            val electionSlot = slot<Election>()
            coEvery { electionRepositoryMock.insert(capture(electionSlot)) } answers { electionSlot.captured.id }

            val result = electionService.createNewElection()

            coVerify(exactly = 1) { electionRepositoryMock.insert(electionSlot.captured) }

            assertEquals(result, electionSlot.captured.id)
        }
    }

    @Nested
    @DisplayName("투표 생성(deleteElection)")
    inner class ElectionDeleteTest {

        @Test
        @DisplayName("투표 삭제")
        fun deleteElection() = runTest {
            val uuid = UUID.randomUUID().toString()
            coEvery { electionRepositoryMock.isExists(uuid) } returns true

            val electionIdSlot = slot<String>()
            coEvery { electionRepositoryMock.deleteById(capture(electionIdSlot)) } returns Unit

            electionService.deleteElection(uuid)

            coVerify(exactly = 1) { electionRepositoryMock.deleteById(electionIdSlot.captured) }

            assertEquals(uuid, electionIdSlot.captured)
        }

        @Test
        @DisplayName("존재하지 않는 투표id를 사용")
        fun electionIsNotExists() = runTest {
            val uuid = UUID.randomUUID().toString()
            coEvery { electionRepositoryMock.isExists(uuid) } returns false

            val exception: ApiException = assertThrows { electionService.deleteElection(uuid) }

            assertEquals(ErrorCode.ELECTION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("모든 투표 조회(getAllElection)")
    inner class GetAllElectionsTest {

        @Test
        @DisplayName("성공적으로 조회시 모든 투표를 Flow로 반환")
        fun getAllElectionsFlow() = runTest {
            val sampleElections = sequence<Election> { Election.new() }.take(5).toList()
            coEvery { electionRepositoryMock.findAll() } returns sampleElections.asFlow()

            val result = electionService.getAllElections().toList()

            coVerify(exactly = 1) { electionRepositoryMock.findAll() }
            assertEquals(sampleElections.count(), result.count())
            assertEquals(sampleElections.map { it.id }, result.map { it.id })
        }
    }

    @Nested
    @DisplayName("페이징 조회 테스트")
    inner class GetElectionsWithPaging {

        @Test
        @DisplayName("[성공 케이스] 특정 페이지의 데이터만 불러옴")
        fun pagingTest() = runTest {
            val pageSlot = slot<Long>()
            val sizeSlot = slot<Long>()

            coEvery {
                electionRepositoryMock.findWithPaging(capture(pageSlot), capture(sizeSlot))
            } answers {
                List(sizeSlot.captured.toInt()) { Election.new() }.asFlow()
            }

            val page = Random.nextLong(10)
            val size = Random.nextLong(10)

            val result = electionService.getElectionsWithPage(page, size)

            assertEquals(size, result.count().toLong())
            assertEquals(page, pageSlot.captured)
            assertEquals(size, sizeSlot.captured)
        }

        @Test
        @DisplayName("[실패 케이스] 페이지가 0미만일 경우 실패")
        fun pagingErrorPageIsIllegal() = runTest {
            val exception: ApiException = assertThrows { electionService.getElectionsWithPage(-1, 10) }

            assertEquals(ErrorCode.PAGING_ERROR, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] 사이즈가 0미만일 경우 실패")
        fun pagingErrorSizeIsIllegal() = runTest {
            val exception: ApiException = assertThrows { electionService.getElectionsWithPage(11, 0) }

            assertEquals(ErrorCode.PAGING_ERROR, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("선거 수 조회")
    inner class GetNumberOfElectionsTest {

        @Test
        @DisplayName("[성공 케이스] 선거 수를 성공적으로 조회")
        fun getNumberOfElectionsTest() = runTest {
            coEvery { electionRepositoryMock.getNumberOfElections() } returns 3

            val result = electionService.getNumberOfElections()

            coVerify(exactly = 1) { electionRepositoryMock.getNumberOfElections() }

            assertEquals(3, result)
        }
    }

    @Nested
    @DisplayName("상태에 따른 선거 수 조회")
    inner class GetCountsByStateTest{

        @Test
        @DisplayName("[성공 케이스] dto에 담아서 반환됨")
        fun getCountsByStateTest() = runTest {
            val numberOfOpened = Random.nextLong(1, 10)
            val numberOfClosed = Random.nextLong(1, 10)

            coEvery { electionRepositoryMock.countByIsOpen() } returns flowOf(
                IsOpenAndCountPairDto(true, numberOfOpened),
                IsOpenAndCountPairDto(false, numberOfClosed),
            )

            val result = electionService.getCountsByState()

            coVerify(exactly = 1) { electionRepositoryMock.countByIsOpen() }

            assertEquals(numberOfOpened, result.opened)
            assertEquals(numberOfClosed, result.closed)
            assertEquals(numberOfOpened + numberOfClosed, result.total)
        }

        @Test
        @DisplayName("[성공 케이스] 어떤 상태의 데이터가 없을 경우 dto에 0이 담겨 반환됨")
        fun getCountsByStateFromElectionsHavingOnlyOneStateTest() = runTest {
            val numberOfElections = Random.nextLong(1, 10)

            coEvery { electionRepositoryMock.countByIsOpen() } returns flowOf(
                IsOpenAndCountPairDto(true, numberOfElections),
            )

            val result = electionService.getCountsByState()

            coVerify(exactly = 1) { electionRepositoryMock.countByIsOpen() }

            assertEquals(numberOfElections, result.opened)
            assertEquals(0, result.closed)
            assertEquals(numberOfElections, result.total)
        }
    }

    @Nested
    @DisplayName("id로 투표 조회(getElection)")
    inner class GetElectionTest {

        @Test
        @DisplayName("성공적으로 조회시 Election를 반환")
        fun getElection() = runTest {
            val uuid = UUID.randomUUID().toString()

            val electionIdSlot = slot<String>()
            coEvery { electionRepositoryMock.findById(capture(electionIdSlot)) } answers { Election(electionIdSlot.captured) }

            val result = electionService.getElection(uuid)

            coVerify(exactly = 1) { electionRepositoryMock.findById(uuid) }
            assertNotNull(result)
            assertEquals(uuid, electionIdSlot.captured)
            assertEquals(uuid, result.id)
        }

        @Test
        @DisplayName("조회하고자 하는 id의 election이 없으면 실패")
        fun getElectionWithWrongId() = runTest {
            val wrongId = UUID.randomUUID().toString()

            coEvery { electionRepositoryMock.findById(any()) } returns null

            val exception: Exception = assertThrows { electionService.getElection(wrongId) }

            coVerify(exactly = 1) { electionRepositoryMock.findById(wrongId) }

            assertInstanceOf<ApiException>(exception)
            assertEquals(ErrorCode.ELECTION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("투표 상태 변경")
    inner class ChangeElectionStateTest {
        @Nested
        @DisplayName("openElection")
        inner class OpenElectionTest {
            @Test
            @DisplayName("성공하는 경우")
            fun openElectionTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val electionMock = spyk<Election>(Election(uuid))

                val electionIdSlot = slot<String>()
                coEvery {
                    electionRepositoryMock.findById(capture(electionIdSlot))
                } answers {
                    electionMock
                }

                val updatedElectionSlot = slot<Election>()
                coEvery {
                    electionRepositoryMock.update(capture(updatedElectionSlot))
                } answers {
                    electionMock
                }

                val result = electionService.openElection(uuid)

                coVerify(exactly = 1) { electionRepositoryMock.findById(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.update(updatedElectionSlot.captured) }
                coVerify(exactly = 1) { electionMock.open() }

                assertEquals(uuid, result.id)
                assertEquals(result.id, electionIdSlot.captured)
                assertEquals(true, result.isOpen)
                assertNotNull(result.startedAt)
            }
        }

        @Nested
        @DisplayName("closeElection")
        inner class CloseElectionTest {
            @Test
            @DisplayName("성공하는 경우")
            fun closeElectionTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val electionMock = spyk<Election>(Election(uuid).apply { open() })

                val electionIdSlot = slot<String>()
                coEvery {
                    electionRepositoryMock.findById(capture(electionIdSlot))
                } answers {
                    electionMock
                }

                val updatedElectionSlot = slot<Election>()
                coEvery {
                    electionRepositoryMock.update(capture(updatedElectionSlot))
                } answers {
                    electionMock
                }

                val result = electionService.closeElection(uuid)

                coVerify(exactly = 1) { electionRepositoryMock.findById(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.update(updatedElectionSlot.captured) }
                coVerify(exactly = 1) { electionMock.close() }

                assertEquals(uuid, result.id)
                assertEquals(result.id, electionIdSlot.captured)
                assertEquals(false, result.isOpen)
                assertNotNull(result.endedAt)
            }
        }

        @Nested
        @DisplayName("changeElectionState")
        inner class ChangeElectionStateTest {
            @Test
            @DisplayName("open election")
            fun openElectionTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val electionMock = spyk<Election>(Election(uuid))

                val electionIdSlot = slot<String>()
                coEvery {
                    electionRepositoryMock.findById(capture(electionIdSlot))
                } answers {
                    electionMock
                }

                val updatedElectionSlot = slot<Election>()
                coEvery {
                    electionRepositoryMock.update(capture(updatedElectionSlot))
                } answers {
                    electionMock
                }

                val result = electionService.changeElectionState(uuid, "open")

                coVerify(exactly = 1) { electionMock.open() }
                coVerify(exactly = 1) { electionService.openElection(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.findById(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.update(updatedElectionSlot.captured) }

                assertEquals(uuid, result.id)
                assertEquals(result.id, electionIdSlot.captured)
                assertEquals(true, result.isOpen)
                assertNotNull(result.startedAt)
            }

            @Test
            @DisplayName("close election")
            fun closeElectionTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val electionMock = spyk<Election>(Election(uuid).apply { open() })

                val electionIdSlot = slot<String>()
                coEvery {
                    electionRepositoryMock.findById(capture(electionIdSlot))
                } answers {
                    electionMock
                }

                val updatedElectionSlot = slot<Election>()
                coEvery {
                    electionRepositoryMock.update(capture(updatedElectionSlot))
                } answers {
                    electionMock
                }

                val result = electionService.changeElectionState(uuid, "close")

                coVerify(exactly = 1) { electionMock.close() }
                coVerify(exactly = 1) { electionService.closeElection(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.findById(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.update(updatedElectionSlot.captured) }

                assertEquals(uuid, result.id)
                assertEquals(result.id, electionIdSlot.captured)
                assertEquals(false, result.isOpen)
                assertNotNull(result.endedAt)
            }

            @Test
            @DisplayName("with wrong election state")
            fun changeElectionStateWithWrongElectionState() = runTest {
                val uuid = UUID.randomUUID().toString()

                val exception: Exception = assertThrows { electionService.changeElectionState(uuid, "wrong state") }

                assertInstanceOf<ApiException>(exception)
                assertEquals(ErrorCode.INVALID_ELECTION_STATE, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("delete expired elections")
        inner class DeleteExpiredElectionsTest {
            val ttlSeconds: Long = 10

            @Test
            @DisplayName("expire")
            fun deleteExpiredElectionsTest() = runTest {
                val cutoffSlot = slot<LocalDateTime>()

                coEvery { electionRepositoryMock.deleteElectionsBeforeCutoff(capture(cutoffSlot)) } returns 3

                val timeBeforeWork = LocalDateTime.now()
                val cutoff = LocalDateTime.now().minusSeconds(ttlSeconds)
                val result = electionService.deleteExpiredElections(cutoff)
                val timeAfterWork = LocalDateTime.now()

                coVerify(exactly = 1) { electionRepositoryMock.deleteElectionsBeforeCutoff(cutoffSlot.captured) }

                assert(cutoffSlot.captured.isAfter(timeBeforeWork.minusSeconds(ttlSeconds)))
                assert(cutoffSlot.captured.isBefore(timeAfterWork.minusSeconds(ttlSeconds)))

                assertEquals(3, result)
            }
        }
    }
}