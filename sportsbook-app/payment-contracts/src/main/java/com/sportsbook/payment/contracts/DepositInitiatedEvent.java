package com.sportsbook.payment.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DepositInitiatedEvent(
        String depositId,
        String playerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String instrumentId,
        Instant createdAt
) {
    public static DepositInitiatedEvent newDeposit(
            String playerId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String instrumentId
    ) {
        return new DepositInitiatedEvent(
                UUID.randomUUID().toString(),
                playerId,
                amount,
                currency,
                paymentMethod,
                instrumentId,
                Instant.now()
        );
    }
}
