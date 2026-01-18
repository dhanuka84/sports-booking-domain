package com.sportsbook.playerservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("/api/playerservice/ping")
    fun ping(): String = "player-service pong"
}
