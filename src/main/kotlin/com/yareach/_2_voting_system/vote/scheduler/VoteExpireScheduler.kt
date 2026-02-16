package com.yareach._2_voting_system.vote.scheduler

import com.yareach._2_voting_system.core.extension.logger
import com.yareach._2_voting_system.vote.service.VoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@ConfigurationProperties(prefix = "vote.expire")
data class VoteExpireProperties(
    var useExpire: Boolean = false,
    var delaySec: Long = 60,
    var ttlSeconds: Long = 600,
)

@Component
@ConditionalOnProperty(prefix = "vote.expire", name = ["use-expire"], havingValue = "true")
class VoteExpireScheduler(
    private val service: VoteService
) {
    private val schedulerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val logger = logger()

    init {
        logger.info("Initializing VoteExpireScheduler")
    }

    @Scheduled(fixedRateString = $$"${vote.expire.delay-sec:60}", timeUnit = TimeUnit.SECONDS)
    fun processDeletingExpiredVotes() {
        schedulerScope.launch {
            try {
                logger.debug("[Scheduler START] Delete Expired Votes Job 시작.")
                val numberOfDeletedVotes = service.deleteExpiredVotes()
                logger.debug("[Scheduler SUCCESS] $numberOfDeletedVotes 개의 만료된 투표 삭제")
            } catch (e: Exception) {
                logger.error("[Scheduler ERROR] 스케줄러 작업중 오류가 발생했습니다.")
                throw e
            }
        }
    }
}