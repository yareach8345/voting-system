package com.yareach.voting_system.slice.vote.repository

import com.yareach.voting_system.election.model.Election
import com.yareach.voting_system.election.repository.ElectionR2dbcRepository
import com.yareach.voting_system.election.repository.ElectionRepository
import com.yareach.voting_system.election.repository.ElectionRepositoryR2dbcImpl
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import java.util.UUID
import kotlin.random.Random
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
    suspend fun initRepository() {
        electionRepository = ElectionRepositoryR2dbcImpl(electionR2dbcRepository)
        electionRepository.deleteAll()
    }

    @Nested
    @DisplayName("모든 데이터 조회 테스트")
    inner class FindAllTest {
        @Test
        @DisplayName("[성공 케이스] 모든 데이터 조회(findAll)")
        fun findAllTest() = runTest {
            electionRepository.insert(Election.new())
            electionRepository.insert(Election.new())

            val result = electionRepository.findAll()

            assertEquals(2, result.count())
        }
    }

    @Nested
    @DisplayName("페이징 테스트")
    inner class FindWithPagingTest {

        @BeforeEach
        suspend fun prepareDates() {
            List(12) { electionRepository.insert(Election.new()) }
        }

        @Test
        @DisplayName("[성공 케이스] 특정 페이지의 선거 정보를 얻어옴")
        fun findByPagingTest() = runTest {
            val firstPageWithSize5 = electionRepository.findWithPaging(0, 5)
            val secondPageWithSize5 = electionRepository.findWithPaging(1, 5)
            val thirdPageWithSize5 = electionRepository.findWithPaging(2, 5)

            assertEquals(5, firstPageWithSize5.count())
            assertEquals(5, secondPageWithSize5.count())
            assertEquals(2, thirdPageWithSize5.count())
        }

        @Test
        @DisplayName("[예외 케이스] 페이지가 오버될 경우 빈 배열을 받아오지만 에러가 발생하지는 않음")
        fun pageOverflowTest() = runTest {
            val thirdPageWithSize5 = electionRepository.findWithPaging(2, 5)
            val fourthPageWithSize5 = electionRepository.findWithPaging(3, 5)

            assertEquals(2, thirdPageWithSize5.count())
            assertEquals(0, fourthPageWithSize5.count())
        }
    }

    @Nested
    @DisplayName("상태에 따른 갯수 출력")
    inner class GetNumberByIsOpenTest {

        @Test
        @DisplayName("[성공 케이스] isOpen에 따라 데이터의 개수를 불러옴")
        fun getNumberOfElectionsByIsOpen() = runTest {
            val numberOfOpenedElections = Random.nextInt(1, 5)
            List(numberOfOpenedElections) { electionRepository.insert(Election.new().apply { open() }) }

            val numberOfClosedElections = Random.nextInt(1, 5)
            List(numberOfClosedElections) { electionRepository.insert(Election.new()) }

            val result = electionRepository.countByIsOpen().toList()
            val map = result.map { it.isOpen to it.count }.toMap()

            assertEquals(2, result.size)
            assertNotNull(map[true])
            assertEquals(numberOfOpenedElections.toLong(), map[true])
            assertNotNull(map[false])
            assertEquals(numberOfClosedElections.toLong(), map[false])
        }

        @Test
        @DisplayName("[성공 케이스] isOpen의 상태 (true, false)중 하나가 존재하지 않을 경우")
        fun getNumberOfElectionsByIsOpenWithElectionsHavingOnlyOneState() = runTest {
            val numberOfOpenedElections = Random.nextInt(1, 10)
            List(numberOfOpenedElections) { electionRepository.insert(Election.new().apply { open() }) }

            val result = electionRepository.countByIsOpen().toList()
            val map = result.map { it.isOpen to it.count }.toMap()

            assertEquals(1, result.size)
            assertNotNull(map[true])
            assertEquals(numberOfOpenedElections.toLong(), map[true])
            assertNull(map[false])
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

        @Test
        @DisplayName("모든 데이터 삭제")
        fun deleteAllTest() = runTest {
            repeat(Random.nextInt(10)) { electionRepository.insert(Election.new()) }

            electionRepository.deleteAll()

            val countAfterDelete = electionRepository.getNumberOfElections()

            assertEquals(0, countAfterDelete)
        }
    }
}