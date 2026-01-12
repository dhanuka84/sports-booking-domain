package com.sportsbook.payment.ingress

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentIngressServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentIngressServiceApplication>(*args)
}
