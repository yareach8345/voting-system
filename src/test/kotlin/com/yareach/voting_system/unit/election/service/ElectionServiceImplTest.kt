package com.yareach.voting_system.unit.election.service

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ElectionServiceImplTest {
    val ttlSeconds: Long = 10

    val electionRepositoryMock = mockk<ElectionRepository>()

    val electionService: ElectionService = spyk(ElectionServiceImpl(electionRepositoryMock, ttlSeconds))

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

            val electionIdSlot = slot<String>()
            coEvery { electionRepositoryMock.deleteById(capture(electionIdSlot)) } returns Unit

            electionService.deleteElection(uuid)

            coVerify(exactly = 1) { electionRepositoryMock.deleteById(electionIdSlot.captured) }

            assertEquals(uuid, electionIdSlot.captured)
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
                val blockSlot = slot<Election.() -> Unit>()
                coEvery {
                    electionRepositoryMock.modify(capture(electionIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(electionMock)
                    electionMock
                }

                val result = electionService.openElection(uuid)

                coVerify(exactly = 1) { electionRepositoryMock.modify(uuid, any()) }
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
                val blockSlot = slot<Election.() -> Unit>()
                coEvery {
                    electionRepositoryMock.modify(capture(electionIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(electionMock)
                    electionMock
                }

                val result = electionService.closeElection(uuid)

                coVerify(exactly = 1) { electionRepositoryMock.modify(uuid, any()) }
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
                val blockSlot = slot<Election.() -> Unit>()
                coEvery {
                    electionRepositoryMock.modify(capture(electionIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(electionMock)
                    electionMock
                }

                val result = electionService.changeElectionState(uuid, "open")

                coVerify(exactly = 1) { electionMock.open() }
                coVerify(exactly = 1) { electionService.openElection(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.modify(uuid, any()) }

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
                val blockSlot = slot<Election.() -> Unit>()
                coEvery {
                    electionRepositoryMock.modify(capture(electionIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(electionMock)
                    electionMock
                }

                val result = electionService.changeElectionState(uuid, "close")

                coVerify(exactly = 1) { electionMock.close() }
                coVerify(exactly = 1) { electionService.closeElection(uuid) }
                coVerify(exactly = 1) { electionRepositoryMock.modify(uuid, any()) }

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
                assertEquals(ErrorCode.ILLEGAL_ELECTION_STATE, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("delete expired elections")
        inner class DeleteExpiredElectionsTest {

            @Test
            @DisplayName("expire")
            fun deleteExpiredElectionsTest() = runTest {
                val cutoffSlot = slot<LocalDateTime>()

                coEvery { electionRepositoryMock.deleteElectionsBeforeCutoff(capture(cutoffSlot)) } returns 3

                val timeBeforeWork = LocalDateTime.now()
                val result = electionService.deleteExpiredElections()
                val timeAfterWork = LocalDateTime.now()

                coVerify(exactly = 1) { electionRepositoryMock.deleteElectionsBeforeCutoff(cutoffSlot.captured) }

                assert(cutoffSlot.captured.isAfter(timeBeforeWork.minusSeconds(ttlSeconds)))
                assert(cutoffSlot.captured.isBefore(timeAfterWork.minusSeconds(ttlSeconds)))

                assertEquals(3, result)
            }
        }
    }
}