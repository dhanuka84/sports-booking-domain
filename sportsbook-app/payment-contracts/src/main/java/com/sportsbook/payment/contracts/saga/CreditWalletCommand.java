package com.sportsbook.payment.contracts.saga;

import java.math.BigDecimal;

public record CreditWalletCommand(
        String sagaId,
        String depositId,
        String playerId,
        java.math.BigDecimal amount
) {}
