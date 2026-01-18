package com.sportsbook.riskservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("/api/riskservice/ping")
    fun ping(): String = "risk-service pong"
}
