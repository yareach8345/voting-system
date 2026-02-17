package com.yareach._2_voting_system.unit.core

import com.yareach._2_voting_system.core.validator.Validator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.aot.hint.TypeReference.listOf
import kotlin.random.Random

class ValidatorTest {

    //가능한 값을 세자리의 숫자로 설정
    val regexString = "[0-9]{3}"

    val validator = Validator(regexString)

    @Test
    @DisplayName("성공 케이스 검사")
    fun testWithValidStrings() {
        val validTexts = List(10) { List(3) { Random.nextInt(0, 10) }.joinToString("") }

        val validResults = validTexts.map { validator.valid(it) }

        assert(validResults.all{ it } )
    }

    @Test
    @DisplayName("실패 케이스 검사")
    fun testWithInvalidStrings() {
        val invalidTexts = listOf(
            "1234", // 자리수 안맞음
            "12", // 자리수 안맞음
            "a13", // 로마자 포함
            "afs", // 로마자 포함
        )

        val validResults = invalidTexts.map { validator.valid(it) }

        assert(validResults.none{ it } )
    }
}