package com.sportsbook.bettingservice.service;

import com.sportsbook.bettingservice.domain.Bet;
import com.sportsbook.bettingservice.domain.BetStatus;
import com.sportsbook.bettingservice.kafka.BetEventMapper;
import com.sportsbook.bettingservice.kafka.BetEventProducer;
import com.sportsbook.bettingservice.kafka.BetPlacedEvent;
import com.sportsbook.bettingservice.repo.BetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final BetEventProducer producer;
    private final BetEventMapper eventMapper;

    /**
     * Keep your current signature. Interpret stake as currency units (e.g. 25.00),
     * and derive stakeCents for persistence.
     */
    @Transactional
    public Bet placeBet(String playerId, String marketId, double stake, double odds) {
        Bet bet = Bet.builder()
                .id(UUID.randomUUID().toString())
                .playerId(playerId)
                .marketId(marketId)
                .stake(stake)
                .stakeCents(stake * 100d)
                .odds(odds)
                .status(BetStatus.PENDING)
                .build();

        betRepository.save(bet);

        // Map domain -> local event via MapStruct, then publish
        BetPlacedEvent event = eventMapper.toLocalEvent(bet);
        producer.publishBetPlaced(event);

        return bet;
    }
}
