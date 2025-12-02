package com.sportsbook.payment.contracts.saga;

public enum DepositSagaStatus {
    STARTED,
    FUNDS_RESERVED,
    VALIDATED_APPROVED,
    VALIDATED_REJECTED,
    WALLET_CREDITED,
    COMPENSATED,
    FAILED
}
