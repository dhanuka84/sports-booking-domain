package com.sportsbook.payment.contracts.saga;

public record ReleaseReservationCommand(
        String sagaId,
        String depositId,
        String providerReservationId
) {}
