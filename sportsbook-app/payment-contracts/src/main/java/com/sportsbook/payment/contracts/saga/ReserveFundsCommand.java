package com.sportsbook.payment.contracts.saga;

import java.math.BigDecimal;

public record ReserveFundsCommand(
        String sagaId,
        String depositId,
        String playerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String instrumentId
) {}
