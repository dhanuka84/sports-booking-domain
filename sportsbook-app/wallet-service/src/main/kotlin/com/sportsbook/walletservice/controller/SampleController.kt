package com.sportsbook.walletservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("/api/walletservice/ping")
    fun ping(): String = "wallet-service pong"
}
