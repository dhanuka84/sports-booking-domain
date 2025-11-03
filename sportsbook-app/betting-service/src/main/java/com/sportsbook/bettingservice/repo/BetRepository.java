package com.sportsbook.bettingservice.repo;

import com.sportsbook.bettingservice.domain.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, String> {
}