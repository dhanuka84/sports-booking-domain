package com.sportsbook.payment.contracts.saga;

import com.sportsbook.payment.contracts.DepositValidatedEvent;

public record DepositValidatedSagaEvent(
        String sagaId,
        DepositValidatedEvent validation
) {}
