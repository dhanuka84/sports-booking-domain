package com.sportsbook.payment.contracts.saga;

import java.time.Instant;

public record ReservationStatusEvent(
        String sagaId,
        String depositId,
        boolean success,
        String providerReservationId,
        String failureReason,
        Instant occurredAt
) {}
