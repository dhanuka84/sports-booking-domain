package com.sportsbook.payment.contracts

enum class DepositRejectionReason {
    CREDIT_CARD,
    OVERDRAFT,
    BNPL_PROVIDER,
    AMBIGUOUS_BIN,
    TECHNICAL_ERROR,
    OTHER,
}
