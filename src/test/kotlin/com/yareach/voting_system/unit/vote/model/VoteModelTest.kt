package com.yareach.voting_system.unit.vote.model

import com.yareach.voting_system.vote.model.Vote
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class VoteModelTest {

    @Nested
    @DisplayName("Factory Method Test")
    inner class FactoryMethodTest{

        @Test
        @DisplayName("팩토리 메서드 of 테스트")
        fun ofTest() {
            val electionId = UUID.randomUUID().toString()

            val datetimeBeforeInit = LocalDateTime.now()
            val newVote = Vote.of(electionId, "testUserId", "testItem")
            val datetimeAfterInit = LocalDateTime.now()

            assertEquals(null, newVote.id)
            assertEquals(electionId, newVote.electionId)
            assertEquals("testUserId", newVote.userId)
            assertEquals("testItem", newVote.item)

            assert(newVote.votedAt.let { it.isAfter(datetimeBeforeInit) && it.isBefore(datetimeAfterInit) })
        }
    }

    @Nested
    @DisplayName("아이템 변경 테스트")
    inner class SetItemTest {

        @Test
        @DisplayName("updateItem으로 아이템 변경")
        fun updateItemTest() {
            val electionId = UUID.randomUUID().toString()

            val newVote = Vote.of(electionId, "testUserId", "testItem")
            val initTime = newVote.votedAt

            newVote.updateItem("updated")

            assertEquals(null, newVote.id)
            assertEquals(electionId, newVote.electionId)
            assertEquals("testUserId", newVote.userId)
            assertEquals("updated", newVote.item)

            assert(newVote.votedAt.isAfter(initTime))
        }
    }
}