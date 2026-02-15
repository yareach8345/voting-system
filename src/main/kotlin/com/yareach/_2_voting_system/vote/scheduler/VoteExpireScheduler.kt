package com.yareach._2_voting_system.vote.scheduler

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

    init {
        println("scheduler bean for vote expired initialized")
    }

    @Scheduled(fixedRateString = $$"${vote.expire.delay-sec:60}", timeUnit = TimeUnit.SECONDS)
    fun processDeletingExpiredVotes() {
        schedulerScope.launch { service.deleteExpiredVotes() }
    }
}