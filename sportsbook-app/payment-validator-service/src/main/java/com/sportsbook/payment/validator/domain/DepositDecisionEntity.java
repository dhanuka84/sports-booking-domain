package com.sportsbook.payment.validator.domain;

import com.sportsbook.payment.contracts.DepositDecision;
import com.sportsbook.payment.contracts.DepositRejectionReason;
import com.sportsbook.payment.contracts.FundingSourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deposit_decision")
public class DepositDecisionEntity {

    @Id
    private String depositId;

    private String playerId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private FundingSourceType fundingSourceType;

    @Enumerated(EnumType.STRING)
    private DepositDecision decision;

    @Enumerated(EnumType.STRING)
    private DepositRejectionReason rejectionReason;

    private String providerReference;

    private Instant decidedAt;

    public String getDepositId() { return depositId; }
    public void setDepositId(String depositId) { this.depositId = depositId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public FundingSourceType getFundingSourceType() { return fundingSourceType; }
    public void setFundingSourceType(FundingSourceType fundingSourceType) { this.fundingSourceType = fundingSourceType; }
    public DepositDecision getDecision() { return decision; }
    public void setDecision(DepositDecision decision) { this.decision = decision; }
    public DepositRejectionReason getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(DepositRejectionReason rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
