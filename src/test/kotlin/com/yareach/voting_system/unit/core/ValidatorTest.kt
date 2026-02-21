package com.yareach.voting_system.unit.core

import com.yareach.voting_system.core.error.ApiException
import com.yareach.voting_system.core.error.ErrorCode
import com.yareach.voting_system.core.validation.Validator
import com.yareach.voting_system.core.validation.ValidatorProperties
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.assertEquals

class ValidatorTest {

    @Nested
    @DisplayName("validatorFromRegexString")
    inner class ValidatorFromRegexString {
        //가능한 값을 세자리의 숫자로 설정
        val regexString = "[0-9]{3}"

        val validator = Validator.fromRegexString(regexString)

        @Test
        @DisplayName("성공 케이스 검사")
        fun testWithIsValidStrings() {
            val validTexts = List(10) { List(3) { Random.nextInt(0, 10) }.joinToString("") }

            val validResults = validTexts.map { validator.isValid(it) }

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

            val validResults = invalidTexts.map { validator.isValid(it) }

            assert(validResults.none{ it } )
        }
    }

    @Nested
    @DisplayName("alwaysTrue")
    inner class AlwaysTrue {
        val alwaysTrue = Validator.alwaysTrue

        @Test
        @DisplayName("어떤 값을 넘기든 항상 True")
        fun testAlwaysTrue() {
            val string = "something word"

            val result = alwaysTrue.isValid(string)

            assert(result)
        }
    }

    @Nested
    @DisplayName("fromOptionsTest")
    inner class FromOptionsTest {

        @Test
        @DisplayName("useValidator = true")
        fun useValidatorTest() {
            val validator = Validator.fromProperties(
                ValidatorProperties.from(true, "[a-z]+")
            )

            val inputs = listOf("abc", "aBc", "1fd", "hello world", "konnichiwa") // "abc"와 "konnichiwa"만 통과

            val countOfValidInputs = inputs.map { validator.isValid(it) }.count{ it }

            assertEquals(2, countOfValidInputs)
        }

        @Test
        @DisplayName("useValidator = false")
        fun useAlwaysTrueTest() {
            val characterList = (('a' .. 'z') + ('A' .. 'Z') + ('1' .. '9')).toList()

            val inputs = List(10) {
                List(Random.nextInt(1, 21)) {
                    characterList[Random.nextInt(0, characterList.size)]
                }.joinToString("")
            }

            val validator = Validator.fromProperties(
                ValidatorProperties.from(false)
            )

            assert(inputs.map { validator.isValid(it) }.all{ it })
        }

        @Test
        @DisplayName("useValidator = true but regexString is null")
        fun invalidPropsTest() {
            val error: ApiException = assertThrows { Validator.fromProperties( ValidatorProperties.from(true) ) }

            assertEquals(ErrorCode.CONFIG_ERROR, error.errorCode)
        }
    }
}