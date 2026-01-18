package com.sportsbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OddsServiceApplication

fun main(args: Array<String>) {
    runApplication<OddsServiceApplication>(*args)
}
