package com.yareach._2_voting_system.unit.election.model

import com.yareach._2_voting_system.election.model.Election
import org.junit.jupiter.api.DisplayName
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElectionTest {
    @Test
    @DisplayName("모델 생성 테스트")
    fun generateTest() {
        val model = Election.new()

        assertEquals(UUID.fromString(model.id).toString(), model.id)
        assertFalse(model.isOpen)
        assertNull(model.startedAt)
        assertNull(model.endedAt)
        assert(model.createdAt.isBefore(LocalDateTime.now()))
    }

    @Test
    @DisplayName("투표 open 테스트")
    fun openElectionTest() {
        val model = Election.new()
            .apply { open() }

        assertTrue(model.isOpen)
        assertNotNull(model.startedAt)
        assert(model.startedAt?.isAfter(model.createdAt) ?: false)
        assert(model.startedAt?.isBefore(LocalDateTime.now()) ?: false)
    }

    @Test
    @DisplayName("투표 close 테스트")
    fun closeElectionTest() {
        val model = Election.new()
            .apply { open() }
            .apply { close() }

        assertFalse(model.isOpen)
        assertNotNull(model.endedAt)
        assert(model.endedAt?.isAfter(model.createdAt) ?: false)
        assert(model.endedAt?.isAfter(model.startedAt) ?: false)
        assert(model.endedAt?.isBefore(LocalDateTime.now()) ?: false)
    }
}