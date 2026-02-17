package com.yareach._2_voting_system.core.validator

interface Validator {
    fun valid(input: String): Boolean
}

class ValidatorImpl(
    regexString: String
) : Validator {
    val validRegex = Regex(regexString)

    override fun valid(input: String): Boolean {
        return validRegex.matches(input)
    }
}

fun Validator(regexString: String): Validator {
    return ValidatorImpl(regexString)
}