package com.yareach.voting_system.election.scheduler

import com.yareach.voting_system.core.extension.logger
import com.yareach.voting_system.election.service.ElectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
@ConfigurationProperties(prefix = "vote.expire")
data class ElectionExpireProperties(
    var useExpire: Boolean = false,
    var delaySec: Long = 60,
    var ttlSeconds: Long = 600,
)

@Component
@ConditionalOnProperty(prefix = "vote.expire", name = ["use-expire"], havingValue = "true")
class ElectionExpireScheduler(
    private val electionExpireProperties: ElectionExpireProperties,
    private val service: ElectionService,
) {
    private val schedulerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val logger = logger()

    init {
        logger.info("Initializing ElectionExpireScheduler")
    }

    @Scheduled(fixedRateString = $$"${vote.expire.delay-sec:60}", timeUnit = TimeUnit.SECONDS)
    fun processDeletingExpiredElections() {
        schedulerScope.launch {
            try {
                logger.debug("[Scheduler START] Delete Expired Election Job 시작.")
                val cutoff = LocalDateTime.now().minusSeconds(electionExpireProperties.ttlSeconds)
                val numberOfDeletedElections = service.deleteExpiredElections(cutoff)
                logger.debug("[Scheduler SUCCESS] $numberOfDeletedElections 개의 만료된 투표 삭제")
            } catch (e: Exception) {
                logger.error("[Scheduler ERROR] 스케줄러 작업중 오류가 발생했습니다.")
                throw e
            }
        }
    }
}