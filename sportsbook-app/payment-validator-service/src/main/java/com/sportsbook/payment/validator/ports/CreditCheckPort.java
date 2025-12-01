package com.sportsbook.payment.validator.ports;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.FundingSourceType;

public interface CreditCheckPort {
    FundingSourceType checkSource(DepositEnrichedEvent depositEnriched);
}
