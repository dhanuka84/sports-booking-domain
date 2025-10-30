package com.sportsbook.oddsservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/oddsservice/ping")
    public String ping() {
        return "odds-service pong";
    }
}
