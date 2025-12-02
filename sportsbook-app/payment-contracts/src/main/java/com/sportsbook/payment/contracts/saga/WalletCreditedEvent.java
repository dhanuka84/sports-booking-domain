package com.sportsbook.payment.contracts.saga;

import java.time.Instant;

public record WalletCreditedEvent(
        String sagaId,
        String depositId,
        String playerId,
        boolean success,
        String failureReason,
        Instant occurredAt
) {}
