package com.yareach._2_voting_system.sample

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestRestController {
    @GetMapping("/test/hello")
    fun hello(): String {
        return "hello, world!"
    }
}