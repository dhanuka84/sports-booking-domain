package com.sportsbook.settlementservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("/api/settlementservice/ping")
    fun ping(): String = "settlement-service pong"
}
