package com.yareach._2_voting_system.core.validation.validator

import com.yareach._2_voting_system.core.validation.Validator
import com.yareach._2_voting_system.core.validation.ValidatorProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "vote.validation.item")
data class ItemValidatorProperties(
    override val useValidator: Boolean = false,
    override val regexString: String? = null
) : ValidatorProperties

@Configuration
class ItemValidation {

    @Bean(name = ["ItemValidator"])
    fun generateItemValidator(
        itemValidatorProperties: ItemValidatorProperties
    ): Validator {
        return Validator.fromProperties(itemValidatorProperties)
    }
}