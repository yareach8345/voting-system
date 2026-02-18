package com.yareach._2_voting_system.core.validation.validator

import com.yareach._2_voting_system.core.validation.Validator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElectionIdValidation {
    //UUID 형식
    val electionIdRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"

    @Bean("ElectionIdValidator")
    fun generateElectionIdValidator() = Validator.fromRegexString(electionIdRegex)
}