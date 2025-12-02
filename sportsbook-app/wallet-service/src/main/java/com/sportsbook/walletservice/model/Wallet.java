package com.sportsbook.walletservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    private String playerId;

    private BigDecimal balance;

    @Version
    private long version;

    protected Wallet() {}

    public Wallet(String playerId, BigDecimal balance) {
        this.playerId = playerId;
        this.balance = balance;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
