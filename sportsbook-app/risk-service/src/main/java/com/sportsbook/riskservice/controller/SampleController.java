package com.sportsbook.riskservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/riskservice/ping")
    public String ping() {
        return "risk-service pong";
    }
}
