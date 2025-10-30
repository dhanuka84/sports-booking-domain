package com.sportsbook.bettingservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/bettingservice/ping")
    public String ping() {
        return "betting-service pong";
    }
}
