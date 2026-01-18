package com.sportsbook.payment.contracts

data class DepositEnrichedEvent(
    val original: DepositInitiatedEvent,
    val riskFlag: DepositRiskFlag,
    val recentCreditBlocks: Int,
    val velocityCountLastHour: Int,
) {
    companion object {
        @JvmStatic
        fun noRisk(original: DepositInitiatedEvent): DepositEnrichedEvent =
            DepositEnrichedEvent(original, DepositRiskFlag.NONE, 0, 0)
    }
}
