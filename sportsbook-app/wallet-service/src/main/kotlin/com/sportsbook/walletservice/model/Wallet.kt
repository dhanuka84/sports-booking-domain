package com.sportsbook.walletservice.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal

@Entity
@Table(name = "wallet")
class Wallet(
    @Id
    var playerId: String? = null,
    var balance: BigDecimal = BigDecimal.ZERO,
) {
    @Version
    var version: Long = 0
}
