package com.sportsbook.bettingservice.repo

import com.sportsbook.bettingservice.domain.Bet
import org.springframework.data.jpa.repository.JpaRepository

interface BetRepository : JpaRepository<Bet, String>
