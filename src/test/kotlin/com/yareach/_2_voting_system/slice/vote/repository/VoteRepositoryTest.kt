package com.yareach._2_voting_system.slice.vote.repository

import com.yareach._2_voting_system.election.model.Election
import com.yareach._2_voting_system.election.repository.ElectionR2dbcRepository
import com.yareach._2_voting_system.election.repository.ElectionRepository
import com.yareach._2_voting_system.election.repository.ElectionRepositoryR2dbcImpl
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteR2dbcRepository
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.repository.VoteRepositoryR2dbcImpl
import kotlinx.coroutines.flow.all
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.none
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

@DataR2dbcTest
class VoteRepositoryTest {
    @Autowired
    private lateinit var electionR2dbcRepository: ElectionR2dbcRepository

    @Autowired
    private lateinit var voteR2dbcRepository: VoteR2dbcRepository

    private lateinit var electionRepository: ElectionRepository

    private lateinit var voteRepository: VoteRepository

    @BeforeTest
    fun setElectionRepository() {
        voteRepository = VoteRepositoryR2dbcImpl(voteR2dbcRepository)

        electionRepository = ElectionRepositoryR2dbcImpl(electionR2dbcRepository)
    }

    @Nested
    @DisplayName("데이터 조회")
    inner class FindTest {

        @Test
        @DisplayName("투표 id와 유저 id로 특정 표 조회")
        fun findByElectionIdAndUserId() = runTest {
            val electionId = electionRepository.insert(Election.new())
            val userId = "testUserId"

            val vote = Vote.of(electionId, userId, "test")
            voteRepository.insert(vote)

            val findResult = voteRepository.findByElectionIdAndUserId(electionId, userId)

            assertNotNull(findResult)
            assertEquals(vote.electionId, findResult.electionId)
            assertEquals(vote.userId, findResult.userId)
            assertEquals(vote.item, findResult.item)
        }

        @Test
        @DisplayName("투표 id로 표 조회")
        fun findByElectionId() = runTest {
            val testSize = 10

            val electionId = electionRepository.insert(Election.new())

            val votes = List(testSize) { index -> Vote.of(electionId, "userId$index", Random.nextInt(0, 10).toString()) }

            votes.forEach { voteRepository.insert(it) }

            val findResults = voteRepository.findAllByElectionId(electionId).map { it.userId to it }.toList().toMap()

            assert(findResults.values.all { it.electionId == electionId })
            assert(votes.all {
                val result = findResults[it.userId]
                result?.electionId == it.electionId && result.userId == it.userId && result.item == it.item
            })
        }
    }

    @Test
    @DisplayName("데이터 추가")
    fun insertTest() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val vote = Vote.of(electionId, "user1", "hello")

        voteRepository.insert(vote)

        val savedVote = voteRepository.findAllByElectionId(electionId).first()

        assertEquals(electionId, savedVote.electionId)
        assertEquals("user1", savedVote.userId)
        assertEquals("hello", savedVote.item)
    }

    @Test
    @DisplayName("데이터 수정")
    fun updateTest() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val firstVoteTime = Vote(null, electionId, "user1", "hello", LocalDateTime.now().minusSeconds(1))
            .also { voteRepository.insert(it) }
            .votedAt

        voteRepository.findAllByElectionId(electionId).first()
            .apply{ updateItem("bye") }
            .also { voteRepository.update(it) }

        val savedVote = voteRepository.findAllByElectionId(electionId).first()

        assertEquals(electionId, savedVote.electionId)
        assertEquals("user1", savedVote.userId)
        assertEquals("bye", savedVote.item)
        assert(savedVote.votedAt.isAfter(firstVoteTime))
    }

    @Test
    @DisplayName("데이터 추가")
    fun findAllByElectionId() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val votes = listOf(
            Vote.of(electionId, "user1", "hello"),
            Vote.of(electionId, "user2", "hello"),
            Vote.of(electionId, "user3", "hello")
        )

        votes.forEach { vote -> voteRepository.insert(vote) }

        val savedVotes = voteRepository.findAllByElectionId(electionId)

        assertEquals(votes.count(), savedVotes.count())
        assert(votes.all { it.electionId == electionId })
    }

    @Test
    @DisplayName("투표id로 모든 투표 삭제")
    fun deleteByElectionIdTest() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val votes = listOf(
            Vote.of(electionId, "user1", "hello"),
            Vote.of(electionId, "user2", "bye"),
            Vote.of(electionId, "user3", "hello")
        )

        votes.forEach { voteRepository.insert(it) }

        val voteCountBeforeDelete = voteRepository.countByElectionId(electionId)

        voteRepository.deleteAllByElectionId(electionId)

        val voteCountAfterDelete = voteRepository.countByElectionId(electionId)

        assertEquals(votes.count().toLong(), voteCountBeforeDelete)
        assertEquals(0, voteCountAfterDelete)
    }

    @Test
    @DisplayName("투표가 삭제되어도 모든 투표기록이 한번에 사라짐")
    fun deleteVotesWhenElectionDeleted() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val votes = listOf(
            Vote.of(electionId, "user1", "hello"),
            Vote.of(electionId, "user2", "bye"),
            Vote.of(electionId, "user3", "hello")
        )

        votes.forEach { voteRepository.insert(it) }

        val voteCountBeforeDelete = voteRepository.countByElectionId(electionId)

        electionRepository.deleteById(electionId)

        val voteCountAfterDelete = voteRepository.countByElectionId(electionId)

        assertEquals(votes.count().toLong(), voteCountBeforeDelete)
        assertEquals(0, voteCountAfterDelete)
    }

    @Test
    @DisplayName("투표 id와 유저 id로 투표기록을 삭제함")
    fun deleteByVoteIdAndUserIdTest() = runTest {
        val electionId = electionRepository.insert(Election.new())

        val votes = listOf(
            Vote.of(electionId, "user1", "hello"),
            Vote.of(electionId, "user2", "bye"),
            Vote.of(electionId, "user3", "hello")
        )

        val userId = votes.random().userId

        votes.forEach { voteRepository.insert(it) }

        voteRepository.deleteByVoteIdAndUserId(electionId, userId)

        val votesAfterDelete = voteRepository.findAllByElectionId(electionId)

        assertEquals(votes.count() - 1, votesAfterDelete.count())
        assert(votesAfterDelete.none { it.userId == userId })
    }

    @Test
    @DisplayName("투표의 투표수 얻기")
    fun countByElectionIdTest() = runTest {
        val testSize = 5
        val numbersOfTestVoteDatas = List(testSize) { Random.nextLong(0, 10) }

        val electionIds = (0 until testSize).map { electionRepository.insert(Election.new()) }

        electionIds.zip(numbersOfTestVoteDatas).forEach { (electionId, numberOfVotes) ->
            repeat((0 until numberOfVotes).count()) {
                voteRepository.insert(
                    Vote.of(
                        electionId,
                        UUID.randomUUID().toString(),
                        Random.nextLong(1, 5).toString()
                    )
                )
            }
        }

        val numbersOfVotes = electionIds.map { voteRepository.countByElectionId(it) }
        assert(numbersOfVotes.zip(numbersOfTestVoteDatas).all { it.first == it.second })
    }

    @Test
    @DisplayName("투표 통계 얻기")
    fun getVoteCountsTest() = runTest {
        val testSize = 25

        val items = List(testSize){ Random.nextLong(0, 10).toString() }

        val voteCounts = items.groupBy { it }.mapValues{ (_, value) -> value.count() }

        val electionId = electionRepository.insert(Election.new())

        items.forEach { voteRepository.insert(Vote.of(electionId, UUID.randomUUID().toString(), it)) }

        val voteCountsFromDb = voteRepository.getVoteCountsByElectionId(electionId)

        assert(
            voteCountsFromDb.all { voteCounts[it.item]?.toLong() == it.voteCount }
        )
    }
}