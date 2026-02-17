package com.yareach._2_voting_system.vote.validator

import com.yareach._2_voting_system.core.extension.logger
import com.yareach._2_voting_system.core.validator.Validator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "vote.validation.user-id")
class UserIdValidatorProperties(
    var useValidator: Boolean = false,
    var regexString: String = ".*"
)

interface UserIdValidator {
    fun valid(input: String): Boolean
}

@Component
@ConditionalOnProperty(prefix = "vote.validation.user-id", name = ["use-validator"], havingValue = "true", matchIfMissing = false)
class UserIdValidatorEnableImpl(
    userIdValidator: UserIdValidatorProperties
) : UserIdValidator {
    private val validator = Validator(userIdValidator.regexString)

    private val logger = logger()

    init {
        logger.info("UserValidator enabled")
    }

    override fun valid(input: String): Boolean {
        return validator.valid(input)
    }
}

@Component
@ConditionalOnProperty(prefix = "vote.validation.user-id", name = ["use-validator"], havingValue = "false", matchIfMissing = true)
class UserIdValidatorDisenableImpl : UserIdValidator {
    override fun valid(input: String): Boolean {
        return true
    }
}
