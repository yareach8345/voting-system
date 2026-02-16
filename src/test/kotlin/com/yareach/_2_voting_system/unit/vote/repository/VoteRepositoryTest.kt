package com.yareach._2_voting_system.unit.vote.repository

import com.yareach._2_voting_system.vote.entity.VoteR2dbcEntity
import com.yareach._2_voting_system.vote.model.Vote
import com.yareach._2_voting_system.vote.repository.VoteR2dbcRepository
import com.yareach._2_voting_system.vote.repository.VoteRepository
import com.yareach._2_voting_system.vote.repository.VoteRepositoryR2dbcImpl
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

class VoteRepositoryTest {
    val voteR2dbcRepositoryMock = mockk<VoteR2dbcRepository>()
    val voteRepository: VoteRepository = VoteRepositoryR2dbcImpl(voteR2dbcRepositoryMock)

    @Nested
    @DisplayName("데이터 조회 테스트")
    inner class VoteRepositoryFindTest {
        @Test
        @DisplayName("모든 데이터 조회(findAll)")
        fun findAllTest() = runTest {
            every { voteR2dbcRepositoryMock.findAll() } returns flowOf(Vote.new(), Vote.new()).map{ VoteR2dbcEntity.fromModel(it) }

            val result = voteRepository.findAll()

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.findAll() }
            assertEquals(result.count(), 2)
        }

        @Test
        @DisplayName("id로 단일 데이터 조회(findById)")
        fun findByIdTest() = runTest {
            val uuid = UUID.randomUUID().toString()
            val idSlot = slot<String>()

            coEvery { voteR2dbcRepositoryMock.findById(id = capture(idSlot)) } answers { VoteR2dbcEntity.fromModel(Vote(idSlot.captured)) }

            val result = voteRepository.findById(uuid)

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.findById(uuid) }
            assertNotNull(result)
            assertEquals(result.id, uuid)
        }

        @Test
        @DisplayName("id로 단일 데이터 조회(findById) 존재하지 않는 id인 경우")
        fun findByIdTestWithNull() = runTest {
            val uuid = UUID.randomUUID().toString()
            val idSlot = slot<String>()

            coEvery { voteR2dbcRepositoryMock.findById(id = capture(idSlot)) } returns null

            val result = voteRepository.findById(uuid)

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.findById(uuid) }
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("데이터 삽입 테스트")
    inner class VoteRepositoryInsertTest {
        @Test
        @DisplayName("데이터 삽입(insert)")
        fun insertTest() = runTest {
            val slot = slot<VoteR2dbcEntity>()
            val uuid = UUID.randomUUID().toString()

            coEvery { voteR2dbcRepositoryMock.save(capture(slot)) } coAnswers { slot.captured }

            val result = voteRepository.insert(Vote(uuid))

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.save(slot.captured) }

            assertEquals(uuid, result)

            assertTrue(slot.captured.isNew)
        }
    }

    @Nested
    @DisplayName("데이터 교체 테스트")
    inner class VoteRepositoryUpdateTest {
        @Test
        @DisplayName("데이터 교체(update)")
        fun updateTest() = runTest {
            val slot = slot<VoteR2dbcEntity>()
            val uuid = UUID.randomUUID().toString()

            coEvery { voteR2dbcRepositoryMock.save(capture(slot)) } coAnswers { slot.captured }

            val updated = voteRepository.update(Vote(uuid))

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.save(slot.captured) }

            assertFalse(slot.captured.isNew)
            assertEquals(updated.id, uuid)
        }
    }

    @Nested
    @DisplayName("데이터 수정 테스트")
    inner class VoteRepositoryModifyTest {
        @Test
        @DisplayName("데이터 수정(modify)")
        fun modifyTest() = runTest {
            val uuid = UUID.randomUUID().toString()

            val voteMock = spyk(Vote(uuid))
            val voteEntityMock = mockk<VoteR2dbcEntity>()
            coEvery { voteEntityMock.toModel() } returns voteMock

            val entitySlot = slot<VoteR2dbcEntity>()
            val idSlot = slot<String>()

            coEvery { voteR2dbcRepositoryMock.findById(capture(idSlot)) } coAnswers { voteEntityMock }
            coEvery { voteR2dbcRepositoryMock.save(capture(entitySlot)) } coAnswers { entitySlot.captured }

            val result = voteRepository.modify(uuid) { open() }

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.findById(uuid) }
            coVerify(exactly = 1) { voteR2dbcRepositoryMock.save(entitySlot.captured) }
            verify(exactly = 1) { voteMock.open() }

            assertFalse(entitySlot.captured.isNew)
            assertTrue(entitySlot.captured.isOpen)

            assertEquals(result.id, uuid)
            assertEquals(result.isOpen, true)
            assertNotNull(result.startedAt)
        }
    }

    @Nested
    @DisplayName("데이터 삭제 테스트")
    inner class VoteRepositoryDeleteTest {
        @Test
        @DisplayName("삭제 테스트(deleteById)")
        fun deleteTest() = runTest {
            val idSlot = slot<String>()

            val uuid = UUID.randomUUID().toString()

            coEvery { voteR2dbcRepositoryMock.deleteById(id = capture(idSlot)) } returns Unit

            voteRepository.deleteById(uuid)

            coVerify(exactly = 1) { voteR2dbcRepositoryMock.deleteById(uuid) }
            assertEquals(uuid, idSlot.captured)
        }
    }
}