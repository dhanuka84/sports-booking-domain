package com.sportsbook.payment.validator.ports

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.FundingSourceType

interface CreditCheckPort {
    fun checkSource(depositEnriched: DepositEnrichedEvent): FundingSourceType
}
