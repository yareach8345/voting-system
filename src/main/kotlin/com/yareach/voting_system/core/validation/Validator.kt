package com.yareach.voting_system.core.validation

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode

interface ValidatorProperties {
    val useValidator: Boolean
    val regexString: String?

    companion object {
        fun from(
            useValidator: Boolean,
            regexString: String? = null,
        ) = object : ValidatorProperties {
            override val useValidator: Boolean
                get() = useValidator
            override val regexString: String?
                get() = regexString
        }
    }
}

fun interface Validator {
    fun isValid(input: String): Boolean

    companion object {
        fun fromProperties(properties: ValidatorProperties): Validator {
            if(!properties.useValidator) {
                return alwaysTrue
            }

            return fromRegexString(
                properties.regexString ?: throw ApiException(ErrorCode.CONFIG_ERROR, "useValidator은 true이나 regexString이 설정되어있지 않습니다.")
            )
        }

        fun fromRegexString(regexString: String): Validator {
            val validRegex = Regex(regexString)
            return Validator { validRegex.matches(it) }
        }

        val alwaysTrue : Validator = Validator { true }
    }
}