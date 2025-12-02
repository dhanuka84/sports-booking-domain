package com.sportsbook.payment.contracts;

public enum DepositRejectionReason {
    CREDIT_CARD,
    OVERDRAFT,
    BNPL_PROVIDER,
    AMBIGUOUS_BIN,
    TECHNICAL_ERROR,
    OTHER
}
