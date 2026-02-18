package com.yareach._2_voting_system.unit.vote.service

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode
import com.yareach._2_voting_system.core.validation.Validator
import com.yareach._2_voting_system.election.model.Election
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.vote.dto.ItemAndVotesCountPairDto
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.core.validation.validator.ItemValidatorProperties
import com.yareach._2_voting_system.core.validation.validator.UserIdValidator
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.service.VoteService
import com.yareach._2_voting_system.vote.service.VoteServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.all
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class VoteServiceTest {

    val itemValidator = Validator.fromProperties(ItemValidatorProperties(true, "[0-9]{2}-[0-9]{2}"))
    val userIdValidator = Validator.fromProperties(UserIdValidator(true, "(user|admin)-[0-9]+"))

    val voteRepositoryMock: VoteRepository = mockk()

    val electionRepositoryMock: ElectionRepository = mockk()

    val voteService: VoteService = VoteServiceImpl(
        voteRepositoryMock,
        electionRepositoryMock,
        itemValidator,
        userIdValidator,
    )

    @Nested
    @DisplayName("record 테스트")
    inner class RecordTest {
        val electionId: String = UUID.randomUUID().toString()

        @Test
        @DisplayName("[성공 케이스] 투표기록을 db에 저장")
        fun recordVoteTest() = runTest {
            val voteSlot = slot<Vote>()

            coEvery {
                electionRepositoryMock.findById(electionId)
            } returns Election(electionId).apply { open() }

            coEvery {
                voteRepositoryMock.insert(capture(voteSlot))
            } answers { voteSlot.captured }

            voteService.record(electionId, "user-1", "01-34")

            assertEquals(electionId, voteSlot.captured.electionId)
            assertEquals("user-1", voteSlot.captured.userId)
            assertEquals("01-34", voteSlot.captured.item)
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 존재하지 않음")
        fun recordVoteWithNotExistsElectionTest() = runTest {
            coEvery {
                electionRepositoryMock.findById(electionId)
            } returns null

            val exception: ApiException = assertThrows { voteService.record(electionId, "user-1", "01-34") }

            assertEquals(ErrorCode.ELECTION_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 열려있지 않음")
        fun recordVoteWithNotOpenedElectionTest() = runTest {
            coEvery {
                electionRepositoryMock.findById(electionId)
            } returns ( Election.new() )

            val exception: ApiException = assertThrows { voteService.record(electionId, "user-1", "01-34") }

            assertEquals(ErrorCode.ELECTION_IS_NOT_OPEN, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] 아이템의 형식이 맞지 않음")
        fun recordVoteWithIllegalItem() = runTest {
            val exception: ApiException = assertThrows { voteService.record(electionId, "user-1", "01-er9") } //허용하지 않는 문자(로마자)가 들어있음

            coVerify(exactly = 0) { electionRepositoryMock.findById(any())  }

            assertEquals(ErrorCode.NOT_VALID_ITEM, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] userId의 형식이 맞지 않음")
        fun recordVoteWithIllegalUserId() = runTest {
            val exception: ApiException = assertThrows { voteService.record(electionId, "tester-1", "12-34") } //유저id 형식이 잘못됨

            coVerify(exactly = 0) { electionRepositoryMock.findById(any())  }

            assertEquals(ErrorCode.NOT_VALID_USERID, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("cancel 테스트")
    inner class CancelVoteTest {

        @Test
        @DisplayName("[성공 케이스] 투표기록 삭제 성공")
        fun cancelVoteTest() = runTest {
            val electionId = UUID.randomUUID().toString()

            val testUserId = "user-1"

            val testVote = Vote.of(electionId, testUserId, "01-01")

            coEvery { voteRepositoryMock.findByElectionIdAndUserId(electionId, testUserId) } returns testVote

            coEvery { voteRepositoryMock.deleteByElectionIdAndUserId(electionId, testUserId) } returns 1

            voteService.cancel(electionId, testUserId)

            coVerify(exactly = 1) { voteRepositoryMock.findByElectionIdAndUserId(electionId, testUserId) }
            coVerify(exactly = 1) { voteRepositoryMock.deleteByElectionIdAndUserId(electionId, testUserId) }
        }

        @Test
        @DisplayName("[실패 케이스] 투표기록이 존재하지 않음")
        fun cancelVoteWithNotFound() = runTest {
            val electionId = UUID.randomUUID().toString()

            val testUserId = "user-1"

            coEvery { voteRepositoryMock.findByElectionIdAndUserId(electionId, testUserId) } returns null

            val exception: ApiException = assertThrows { voteService.cancel(electionId, testUserId) }

            coVerify(exactly = 1) { voteRepositoryMock.findByElectionIdAndUserId(electionId, testUserId) }

            assertEquals(ErrorCode.VOTE_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] 유저id가 잘못됨")
        fun cancelVoteWithIllegalUserId() = runTest {
            val electionId = UUID.randomUUID().toString()

            val testUserId = "tester-1"

            val exception: ApiException = assertThrows { voteService.cancel(electionId, testUserId) }

            coVerify(exactly = 0) { voteRepositoryMock.findByElectionIdAndUserId(electionId, testUserId) }

            assertEquals(ErrorCode.NOT_VALID_USERID, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("changeItem 테스트")
    inner class ChangeItemTest {

        @Test
        @DisplayName("[성공 케이스] 새로운 item으로 업데이트 됨")
        fun successCaseItemIsChanged() = runTest {
            val testVoteId = Random.nextInt()
            val electionId = UUID.randomUUID().toString()
            val userId = "user-001"

            val testVote = Vote(testVoteId, electionId, userId, "01-01")

            val updatedVoteSlot = slot<Vote>()

            coEvery { voteRepositoryMock.findByElectionIdAndUserId(electionId, userId) } returns testVote

            coEvery { voteRepositoryMock.update(capture(updatedVoteSlot)) } answers { updatedVoteSlot.captured }

            voteService.changeItem(electionId, userId, "05-05")

            coVerify(exactly = 1) { voteRepositoryMock.findByElectionIdAndUserId(electionId, userId) }

            coVerify(exactly = 1) { voteRepositoryMock.update(updatedVoteSlot.captured) }

            assertEquals(testVoteId, updatedVoteSlot.captured.id)
            assertEquals(electionId, updatedVoteSlot.captured.electionId)
            assertEquals(userId, updatedVoteSlot.captured.userId)
            assertEquals("05-05", updatedVoteSlot.captured.item)
        }

        @Test
        @DisplayName("[실패 케이스] 해당하는 투표기록이 존재하지 않음")
        fun failCaseItemVoteNotExists() = runTest {
            val electionId = UUID.randomUUID().toString()
            val userId = "user-001"

            coEvery { voteRepositoryMock.findByElectionIdAndUserId(electionId, userId) } returns null

            val exception: ApiException = assertThrows { voteService.changeItem(electionId, userId, "05-05") }

            coVerify(exactly = 1) { voteRepositoryMock.findByElectionIdAndUserId(electionId, userId) }

            coVerify(exactly = 0) { voteRepositoryMock.update(any()) }

            assertEquals(ErrorCode.VOTE_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] item의 형식이 잘못됨")
        fun failCaseInvalidItem() = runTest {
            val electionId = UUID.randomUUID().toString()
            val userId = "user-001"

            val exception: ApiException = assertThrows { voteService.changeItem(electionId, userId, "05-aa") }

            assertEquals(ErrorCode.NOT_VALID_ITEM, exception.errorCode)
        }

        @Test
        @DisplayName("[실패 케이스] user id의 형식이 잘못됨")
        fun failCaseInvalidUserId() = runTest {
            val electionId = UUID.randomUUID().toString()
            val userId = "someone-001"

            val exception: ApiException = assertThrows { voteService.changeItem(electionId, userId, "12-34") }

            assertEquals(ErrorCode.NOT_VALID_USERID, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("투표 id로 투표기록 삭제")
    inner class DeleteVotesByElectionIdTest {

        @Test
        @DisplayName("[성공 케이스] 투표 id로 투표기록 삭제 요청")
        fun successCaseVotesIsDeleted() = runTest {
            val testElection = Election.new()
            val electionId = testElection.id

            val numberOfDeletedVotes = Random.nextLong(1, 30)

            coEvery { electionRepositoryMock.findById(electionId) } returns testElection

            coEvery { voteRepositoryMock.deleteAllByElectionId(electionId) } returns numberOfDeletedVotes

            val result = voteService.deleteByElectionId(electionId)

            coVerify(exactly = 1) { voteRepositoryMock.deleteAllByElectionId(electionId) }

            assertEquals(numberOfDeletedVotes, result)
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 존재하지 않아 실패")
        fun failCaseElectionIsNotExists() = runTest {
            val electionId = UUID.randomUUID().toString()

            coEvery { electionRepositoryMock.findById(electionId) } returns null

            val exception: ApiException = assertThrows { voteService.deleteByElectionId(electionId) }

            assertEquals(ErrorCode.ELECTION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("투표의 현재 통계 얻기")
    inner class GetElectionStatisticsTest {

        @Test
        @DisplayName("[성공 케이스] 통계 얻어오기 성공")
        fun successCaseGetStatistics() = runTest {
            val election = Election.new()
            val electionId = election.id

            val testStatistics = flowOf(
                ItemAndVotesCountPairDto("01-01", 2),
                ItemAndVotesCountPairDto("01-02", 5),
                ItemAndVotesCountPairDto("10-01", 3),
                ItemAndVotesCountPairDto("01-10", 4),
            )

            val testStatisticsMap = testStatistics.map { it.item to it.voteCount }.toList().toMap()

            coEvery { electionRepositoryMock.findById(electionId) } returns election

            coEvery { voteRepositoryMock.getVoteCountsByElectionId(electionId) } returns testStatistics

            val result = voteService.getElectionStatistics(electionId)

            coVerify(exactly = 1) { electionRepositoryMock.findById(electionId) }
            coVerify(exactly = 1) { voteRepositoryMock.getVoteCountsByElectionId(electionId) }

            assert(result.map { testStatisticsMap[it.item] == it.voteCount }.all { it })
        }

        @Test
        @DisplayName("[실패 케이스] 투표가 존재하지 않음")
        fun failCaseElectionNotExists() = runTest {
            coEvery { electionRepositoryMock.findById(any()) } returns null

            val exception: ApiException = assertThrows { voteService.getElectionStatistics(UUID.randomUUID().toString()).toList() }

            coVerify(exactly = 1) { electionRepositoryMock.findById(any()) }

            assertEquals(ErrorCode.ELECTION_NOT_FOUND, exception.errorCode)
        }
    }
}