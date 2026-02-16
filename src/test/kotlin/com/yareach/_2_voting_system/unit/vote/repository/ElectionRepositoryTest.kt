package com.yareach._2_voting_system.unit.vote.repository

import com.yareach._2_voting_system.vote.entity.ElectionR2dbcEntity
import com.yareach._2_voting_system.vote.model.Election
import com.yareach._2_voting_system.vote.repository.ElectionR2dbcRepository
import com.yareach._2_voting_system.vote.repository.ElectionRepository
import com.yareach._2_voting_system.vote.repository.ElectionRepositoryR2DbcImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElectionRepositoryTest {
    val electionR2DbcRepositoryMock = mockk<ElectionR2dbcRepository>()
    val electionRepository: ElectionRepository = ElectionRepositoryR2DbcImpl(electionR2DbcRepositoryMock)

    @Nested
    @DisplayName("데이터 조회 테스트")
    inner class ElectionRepositoryFindTest {
        @Test
        @DisplayName("모든 데이터 조회(findAll)")
        fun findAllTest() = runTest {
            every { electionR2DbcRepositoryMock.findAll() } returns flowOf(Election.new(), Election.new()).map{ ElectionR2dbcEntity.fromModel(it) }

            val result = electionRepository.findAll()

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.findAll() }
            assertEquals(result.count(), 2)
        }

        @Test
        @DisplayName("id로 단일 데이터 조회(findById)")
        fun findByIdTest() = runTest {
            val uuid = UUID.randomUUID().toString()
            val idSlot = slot<String>()

            coEvery { electionR2DbcRepositoryMock.findById(id = capture(idSlot)) } answers { ElectionR2dbcEntity.fromModel(Election(idSlot.captured)) }

            val result = electionRepository.findById(uuid)

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.findById(uuid) }
            assertNotNull(result)
            assertEquals(result.id, uuid)
        }

        @Test
        @DisplayName("id로 단일 데이터 조회(findById) 존재하지 않는 id인 경우")
        fun findByIdTestWithNull() = runTest {
            val uuid = UUID.randomUUID().toString()
            val idSlot = slot<String>()

            coEvery { electionR2DbcRepositoryMock.findById(id = capture(idSlot)) } returns null

            val result = electionRepository.findById(uuid)

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.findById(uuid) }
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("데이터 삽입 테스트")
    inner class ElectionRepositoryInsertTest {
        @Test
        @DisplayName("데이터 삽입(insert)")
        fun insertTest() = runTest {
            val slot = slot<ElectionR2dbcEntity>()
            val uuid = UUID.randomUUID().toString()

            coEvery { electionR2DbcRepositoryMock.save(capture(slot)) } coAnswers { slot.captured }

            val result = electionRepository.insert(Election(uuid))

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.save(slot.captured) }

            assertEquals(uuid, result)

            assertTrue(slot.captured.isNew)
        }
    }

    @Nested
    @DisplayName("데이터 교체 테스트")
    inner class ElectionRepositoryUpdateTest {
        @Test
        @DisplayName("데이터 교체(update)")
        fun updateTest() = runTest {
            val slot = slot<ElectionR2dbcEntity>()
            val uuid = UUID.randomUUID().toString()

            coEvery { electionR2DbcRepositoryMock.save(capture(slot)) } coAnswers { slot.captured }

            val updated = electionRepository.update(Election(uuid))

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.save(slot.captured) }

            assertFalse(slot.captured.isNew)
            assertEquals(updated.id, uuid)
        }
    }

    @Nested
    @DisplayName("데이터 수정 테스트")
    inner class ElectionRepositoryModifyTest {
        @Test
        @DisplayName("데이터 수정(modify)")
        fun modifyTest() = runTest {
            val uuid = UUID.randomUUID().toString()

            val electionMock = spyk(Election(uuid))
            val electionEntityMock = mockk<ElectionR2dbcEntity>()
            coEvery { electionEntityMock.toModel() } returns electionMock

            val entitySlot = slot<ElectionR2dbcEntity>()
            val idSlot = slot<String>()

            coEvery { electionR2DbcRepositoryMock.findById(capture(idSlot)) } coAnswers { electionEntityMock }
            coEvery { electionR2DbcRepositoryMock.save(capture(entitySlot)) } coAnswers { entitySlot.captured }

            val result = electionRepository.modify(uuid) { open() }

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.findById(uuid) }
            coVerify(exactly = 1) { electionR2DbcRepositoryMock.save(entitySlot.captured) }
            verify(exactly = 1) { electionMock.open() }

            assertFalse(entitySlot.captured.isNew)
            assertTrue(entitySlot.captured.isOpen)

            assertEquals(result.id, uuid)
            assertEquals(result.isOpen, true)
            assertNotNull(result.startedAt)
        }
    }

    @Nested
    @DisplayName("데이터 삭제 테스트")
    inner class ElectionRepositoryDeleteTest {
        @Test
        @DisplayName("삭제 테스트(deleteById)")
        fun deleteTest() = runTest {
            val idSlot = slot<String>()

            val uuid = UUID.randomUUID().toString()

            coEvery { electionR2DbcRepositoryMock.deleteById(id = capture(idSlot)) } returns Unit

            electionRepository.deleteById(uuid)

            coVerify(exactly = 1) { electionR2DbcRepositoryMock.deleteById(uuid) }
            assertEquals(uuid, idSlot.captured)
        }
    }
}