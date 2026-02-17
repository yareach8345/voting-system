package com.yareach._2_voting_system.vote.properties

import com.yareach._2_voting_system.core.validator.ValidatorProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vote.validation.user-id")
data class UserIdValidatorProperties(
    override val useValidator: Boolean = false,
    override val regexString: String? = null
) : ValidatorProperties