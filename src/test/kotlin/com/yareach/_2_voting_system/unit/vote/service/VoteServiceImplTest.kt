package com.yareach._2_voting_system.unit.vote.service

import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.service.VoteService
import com.yareach._2_voting_system.vote.service.VoteServiceImpl
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
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VoteServiceImplTest {
    val voteRepositoryMock = mockk<VoteRepository>()

    val voteService: VoteService = spyk(VoteServiceImpl(voteRepositoryMock))

    @Nested
    @DisplayName("새 투표 생성(createNewVote)")
    inner class VoteCreateTest {

        @Test
        @DisplayName("새 투표 생성, db에 저장")
        fun createNewVoteTest() = runTest {

            val voteSlot = slot<Vote>()
            coEvery { voteRepositoryMock.insert(capture(voteSlot)) } answers { voteSlot.captured.id }

            val result = voteService.createNewVote()

            coVerify(exactly = 1) { voteRepositoryMock.insert(voteSlot.captured) }

            assertEquals(result, voteSlot.captured.id)
        }
    }

    @Nested
    @DisplayName("투표 생성(deleteVote)")
    inner class VoteDeleteTest {

        @Test
        @DisplayName("투표 삭제")
        fun deleteVote() = runTest {
            val uuid = UUID.randomUUID().toString()

            val voteIdSlot = slot<String>()
            coEvery { voteRepositoryMock.deleteById(capture(voteIdSlot)) } returns Unit

            voteService.deleteVote(uuid)

            coVerify(exactly = 1) { voteRepositoryMock.deleteById(voteIdSlot.captured) }

            assertEquals(uuid, voteIdSlot.captured)
        }
    }

    @Nested
    @DisplayName("모든 투표 조회(getAllVotes)")
    inner class GetAllVotesTest {

        @Test
        @DisplayName("성공적으로 조회시 모든 투표를 Flow로 반환")
        fun getAllVotesFlow() = runTest {
            val sampleVotes = sequence<Vote> { Vote.new() }.take(5).toList()
            coEvery { voteRepositoryMock.findAll() } returns sampleVotes.asFlow()

            val result = voteService.getAllVotes().toList()

            coVerify(exactly = 1) { voteRepositoryMock.findAll() }
            assertEquals(sampleVotes.count(), result.count())
            assertEquals(sampleVotes.map { it.id }, result.map { it.id })
        }
    }

    @Nested
    @DisplayName("id로 투표 조회(getVote)")
    inner class GetVoteTest {

        @Test
        @DisplayName("성공적으로 조회시 Vote를 반환")
        fun getVote() = runTest {
            val uuid = UUID.randomUUID().toString()

            val voteIdSlot = slot<String>()
            coEvery { voteRepositoryMock.findById(capture(voteIdSlot)) } answers { Vote(voteIdSlot.captured) }

            val result = voteService.getVote(uuid)

            coVerify(exactly = 1) { voteRepositoryMock.findById(uuid) }
            assertNotNull(result)
            assertEquals(uuid, voteIdSlot.captured)
            assertEquals(uuid, result.id)
        }

        @Test
        @DisplayName("조회하고자 하는 id의 vote가 없으면 실패")
        fun getVoteWithWrongId() = runTest {
            val wrongId = UUID.randomUUID().toString()

            coEvery { voteRepositoryMock.findById(any()) } returns null

            val exception: Exception = assertThrows { voteService.getVote(wrongId) }

            coVerify(exactly = 1) { voteRepositoryMock.findById(wrongId) }
            assertInstanceOf<NotFoundException>(exception)
        }
    }

    @Nested
    @DisplayName("투표 상태 변경")
    inner class ChangeVoteStateTest {
        @Nested
        @DisplayName("openVote")
        inner class OpenVoteTest {
            @Test
            @DisplayName("성공하는 경우")
            fun openVoteTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val voteMock = spyk<Vote>(Vote(uuid))

                val voteIdSlot = slot<String>()
                val blockSlot = slot<Vote.() -> Unit>()
                coEvery {
                    voteRepositoryMock.modify(capture(voteIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(voteMock)
                    voteMock
                }

                val result = voteService.openVote(uuid)

                coVerify(exactly = 1) { voteRepositoryMock.modify(uuid, any()) }
                coVerify(exactly = 1) { voteMock.open() }

                assertEquals(uuid, result.id)
                assertEquals(result.id, voteIdSlot.captured)
                assertEquals(true, result.isOpen)
                assertNotNull(result.startedAt)
            }
        }

        @Nested
        @DisplayName("closeVote")
        inner class CloseVoteTest {
            @Test
            @DisplayName("성공하는 경우")
            fun closeVoteTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val voteMock = spyk<Vote>(Vote(uuid).apply { open() })

                val voteIdSlot = slot<String>()
                val blockSlot = slot<Vote.() -> Unit>()
                coEvery {
                    voteRepositoryMock.modify(capture(voteIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(voteMock)
                    voteMock
                }

                val result = voteService.closeVote(uuid)

                coVerify(exactly = 1) { voteRepositoryMock.modify(uuid, any()) }
                coVerify(exactly = 1) { voteMock.close() }

                assertEquals(uuid, result.id)
                assertEquals(result.id, voteIdSlot.captured)
                assertEquals(false, result.isOpen)
                assertNotNull(result.endedAt)
            }
        }

        @Nested
        @DisplayName("changeVoteState")
        inner class ChangeVoteStateTest {
            @Test
            @DisplayName("open vote")
            fun openVoteTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val voteMock = spyk<Vote>(Vote(uuid))

                val voteIdSlot = slot<String>()
                val blockSlot = slot<Vote.() -> Unit>()
                coEvery {
                    voteRepositoryMock.modify(capture(voteIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(voteMock)
                    voteMock
                }

                val result = voteService.changeVoteState(uuid, "open")

                coVerify(exactly = 1) { voteMock.open() }
                coVerify(exactly = 1) { voteService.openVote(uuid) }
                coVerify(exactly = 1) { voteRepositoryMock.modify(uuid, any()) }

                assertEquals(uuid, result.id)
                assertEquals(result.id, voteIdSlot.captured)
                assertEquals(true, result.isOpen)
                assertNotNull(result.startedAt)
            }

            @Test
            @DisplayName("close vote")
            fun closeVoteTest() = runTest {
                val uuid = UUID.randomUUID().toString()
                val voteMock = spyk<Vote>(Vote(uuid).apply { open() })

                val voteIdSlot = slot<String>()
                val blockSlot = slot<Vote.() -> Unit>()
                coEvery {
                    voteRepositoryMock.modify(capture(voteIdSlot), capture(blockSlot))
                } answers {
                    blockSlot.captured(voteMock)
                    voteMock
                }

                val result = voteService.changeVoteState(uuid, "close")

                coVerify(exactly = 1) { voteMock.close() }
                coVerify(exactly = 1) { voteService.closeVote(uuid) }
                coVerify(exactly = 1) { voteRepositoryMock.modify(uuid, any()) }

                assertEquals(uuid, result.id)
                assertEquals(result.id, voteIdSlot.captured)
                assertEquals(false, result.isOpen)
                assertNotNull(result.endedAt)
            }

            @Test
            @DisplayName("with wrong vote state")
            fun changeVoteStateWithWrongVoteState() = runTest {
                val uuid = UUID.randomUUID().toString()

                val exception: Error = assertThrows { voteService.changeVoteState(uuid, "wrong state") }

                assertInstanceOf<Error>(exception)
            }
        }
    }
}