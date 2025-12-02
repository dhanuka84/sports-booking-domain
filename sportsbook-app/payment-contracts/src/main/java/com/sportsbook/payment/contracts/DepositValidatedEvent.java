package com.sportsbook.payment.contracts;

import java.math.BigDecimal;
import java.time.Instant;

public record DepositValidatedEvent(
        String depositId,
        String playerId,
        BigDecimal amount,
        FundingSourceType fundingSourceType,
        DepositDecision decision,
        DepositRejectionReason rejectionReason,
        String providerReference,
        Instant decidedAt
) {}
