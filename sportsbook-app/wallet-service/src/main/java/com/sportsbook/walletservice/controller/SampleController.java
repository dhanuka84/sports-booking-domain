package com.sportsbook.walletservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/api/walletservice/ping")
    public String ping() {
        return "wallet-service pong";
    }
}
