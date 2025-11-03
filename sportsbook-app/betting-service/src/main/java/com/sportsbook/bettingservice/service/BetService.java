package com.sportsbook.bettingservice.service;

import com.sportsbook.bettingservice.api.dto.PlaceBetRequest;
import com.sportsbook.bettingservice.domain.Bet;
import com.sportsbook.bettingservice.domain.BetStatus;
import com.sportsbook.bettingservice.domain.OutboxEvent;
import com.sportsbook.bettingservice.repo.BetRepository;
import com.sportsbook.bettingservice.repo.OutboxRepository;
import com.sportsbook.events.BetPlaced;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static com.sportsbook.bettingservice.outbox.OutboxUtil.avroBytes;

@Service
@RequiredArgsConstructor
public class BetService {
    private final BetRepository bets;
    private final OutboxRepository outbox;

    @Transactional
    public String place(PlaceBetRequest req) {
        String betId = UUID.randomUUID().toString();
        Bet bet = Bet.builder().betId(betId).userId(req.userId()).eventId(req.eventId()).marketId(req.marketId()).outcomeId(req.outcomeId()).stake(req.stake()).odds(req.odds()).status(BetStatus.PENDING).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        bets.save(bet);
        BetPlaced evt = BetPlaced.newBuilder().setEventId(req.eventId()).setBetId(betId).setUserId(req.userId()).setMarketId(req.marketId()).setOutcomeId(req.outcomeId()).setStake(req.stake()).setOdds(req.odds()).setTimestamp(Instant.now().toEpochMilli()).build();
        try {
            var ob = OutboxEvent.builder().topic("bets.placed").key(betId).payload(avroBytes(evt)).schemaSubject("com.sportsbook.events.BetPlaced").schemaVersion(1).status("PENDING").createdAt(Instant.now()).build();
            outbox.save(ob);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return betId;
    }
}