package com.yareach.voting_system.slice.vote.repository

import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionR2dbcRepository
import com.yareach.voting_system.election.repository.ElectionRepository
import com.yareach.voting_system.election.repository.ElectionRepositoryR2dbcImpl
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataR2dbcTest
class ElectionRepositoryTest {
    @Autowired
    private lateinit var electionR2dbcRepository: ElectionR2dbcRepository

    private lateinit var electionRepository: ElectionRepository

    @BeforeEach
    fun initRepository() {
        electionRepository = ElectionRepositoryR2dbcImpl(electionR2dbcRepository)
    }

    @Nested
    @DisplayName("모즌 데이터 조회 테스트")
    inner class FindAllTest {
        @Test
        @DisplayName("[성공 케이스] 모든 데이터 조회(findAll)")
        fun findAllTest() = runTest {
            electionRepository.insert(Election.new())
            electionRepository.insert(Election.new())

            val result = electionRepository.findAll()

            assert(result.count() >= 2)
        }
    }

    @Nested
    @DisplayName("id로 특정 투표만 조회")
    inner class FindByIdTest {

        @Test
        @DisplayName("[성공 케이스] id로 단일 데이터 조회(findById)")
        fun findByIdTest() = runTest {
            val election = Election.new()
            val electionId = election.id

            electionRepository.insert(election)

            val result = electionRepository.findById(electionId)

            assertNotNull(result)
            assertEquals(result.id, electionId)
        }

        @Test
        @DisplayName("[실페 케이스] 존재하지 않는 id인 경우")
        fun findByIdTestWithNull() = runTest {
            val notExistUuid = UUID.randomUUID().toString()

            val result = electionRepository.findById(notExistUuid)

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("존재여부 확인")
    inner class ExistsTest {

        @Test
        @DisplayName("존재하는 투표로 확인")
        fun withExistingElection() = runTest {
            val election = Election.new()
            val electionId = election.id

            electionRepository.insert(election)

            val result = electionRepository.isExists(electionId)

            assertEquals(true, result)
        }

        @Test
        @DisplayName("존재하지 않는 투표 id로 확인")
        fun withNotExistingElection() = runTest {
            val notExistElectionId = UUID.randomUUID().toString()

            val result = electionRepository.isExists(notExistElectionId)

            assertEquals(false, result)
        }
    }

    @Nested
    @DisplayName("isOpen 조회")
    inner class FindIsOpen {

        @Test
        @DisplayName("열려있는 투표 조회")
        fun electionIsOpened() = runTest {
            val election = Election.new().apply{ open() }
            val electionId = election.id

            electionRepository.insert(election)

            val result = electionRepository.getIsOpen(electionId)

            assertNotNull(result)
            assertEquals(true, result)
        }

        @Test
        @DisplayName("닫혀있는 투표 조회")
        fun electionIsClosed() = runTest {
            val election = Election.new()
            val electionId = election.id

            electionRepository.insert(election)

            val result = electionRepository.getIsOpen(electionId)

            assertNotNull(result)
            assertEquals(false, result)
        }

        @Test
        @DisplayName("존재하지 않는 투표 조회")
        fun electionIsNotExists() = runTest {
            val notExistElectionId = UUID.randomUUID().toString()

            val result = electionRepository.getIsOpen(notExistElectionId)

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("데이터 삽입 테스트")
    inner class ElectionRepositoryInsertTest {
        @Test
        @DisplayName("데이터 삽입(insert)")
        fun insertTest() = runTest {
            val election = Election.new()
            val electionId = election.id

            val result = electionRepository.insert(election)

            val findResult = electionRepository.findById(electionId)

            assertEquals(electionId, result)
            assertEquals(electionId, findResult?.id)
        }
    }

    @Nested
    @DisplayName("데이터 교체 테스트")
    inner class ElectionRepositoryUpdateTest {
        @Test
        @DisplayName("데이터 교체(update)")
        fun updateTest() = runTest {
            val election = Election.new()
            val electionId = election.id

            electionRepository.insert(election)

            election.open()

            val updated = electionRepository.update(election)

            assertEquals(electionId, updated.id)
            assertEquals(true, updated.isOpen)
        }
    }

    @Nested
    @DisplayName("데이터 삭제 테스트")
    inner class ElectionRepositoryDeleteTest {
        @Test
        @DisplayName("삭제 테스트(deleteById)")
        fun deleteTest() = runTest {
            val election = Election.new()
            val electionId = election.id

            electionRepository.insert(election)

            electionRepository.deleteById(electionId)

            val findVoteResultAfter = electionRepository.findById(electionId)

            assertNull(findVoteResultAfter)
        }
    }
}