package com.yareach._2_voting_system.sample

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TestController {
    @GetMapping("/test/html")
    fun html(
        model: Model,
        @RequestParam message: String?
    ): String {
        model.addAttribute("message", message ?: "default message")
        return "/test"
    }
}