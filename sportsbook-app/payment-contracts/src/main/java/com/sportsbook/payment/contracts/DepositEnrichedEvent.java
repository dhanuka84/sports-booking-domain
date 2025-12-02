package com.sportsbook.payment.contracts;

public record DepositEnrichedEvent(
        DepositInitiatedEvent original,
        DepositRiskFlag riskFlag,
        int recentCreditBlocks,
        int velocityCountLastHour
) {
    public static DepositEnrichedEvent noRisk(DepositInitiatedEvent original) {
        return new DepositEnrichedEvent(original, DepositRiskFlag.NONE, 0, 0);
    }
}
