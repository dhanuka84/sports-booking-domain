package com.sportsbook.payment.validator.domain

import com.sportsbook.payment.contracts.DepositDecision
import com.sportsbook.payment.contracts.DepositRejectionReason
import com.sportsbook.payment.contracts.FundingSourceType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "deposit_decision")
class DepositDecisionEntity {

    @Id
    var depositId: String? = null

    var playerId: String? = null

    var amount: BigDecimal? = null

    @Enumerated(EnumType.STRING)
    var fundingSourceType: FundingSourceType? = null

    @Enumerated(EnumType.STRING)
    var decision: DepositDecision? = null

    @Enumerated(EnumType.STRING)
    var rejectionReason: DepositRejectionReason? = null

    var providerReference: String? = null

    var decidedAt: Instant? = null
}
