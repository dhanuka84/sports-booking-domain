package com.sportsbook.playerservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/playerservice/ping")
    public String ping() {
        return "player-service pong";
    }
}
