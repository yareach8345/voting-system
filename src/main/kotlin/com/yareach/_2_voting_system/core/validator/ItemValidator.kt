package com.yareach._2_voting_system.core.validator

import com.yareach._2_voting_system.core.error.ApiException
import com.yareach._2_voting_system.core.error.ErrorCode

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
    fun valid(input: String): Boolean

    companion object {
        fun fromProperties( properties: ValidatorProperties ) =
            when (properties.useValidator) {
                true -> properties.regexString?.let{ fromRegexString(it) } ?: throw ApiException(ErrorCode.INVALID_PROP, "useValidator은 true이나 regexString이 설정되어있지 않습니다.")
                false -> alwaysTrue
            }

        fun fromRegexString(regexString: String): Validator {
            val validRegex = Regex(regexString)
            return Validator { validRegex.matches(it) }
        }

        val alwaysTrue : Validator = Validator { true }
    }
}