package com.sportsbook.settlementservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/settlementservice/ping")
    public String ping() {
        return "settlement-service pong";
    }
}
