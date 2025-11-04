package com.sportsbook.bettingservice.kafka;

import com.sportsbook.bettingservice.domain.Bet;
import com.sportsbook.events.BetPlaced;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BetEventMapper {

    // existing: local event -> Avro (used by BetEventProducer)
    @Mapping(target = "eventId",   expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "betId",     source = "id")
    @Mapping(target = "playerId",  source = "playerId")
    @Mapping(target = "userId",    source = "userId")
    @Mapping(target = "marketId",  source = "matchId")     // matchId -> marketId
    @Mapping(target = "outcomeId", source = "selection")   // selection -> outcomeId
    @Mapping(target = "stake",     source = "stake")
    @Mapping(target = "odds",      source = "odds")
    @Mapping(target = "timestamp", expression = "java(System.currentTimeMillis())")
    BetPlaced toAvro(BetPlacedEvent source);

    // NEW: domain Bet -> local BetPlacedEvent (used by service)
    @Mapping(target = "id",        source = "id")
    @Mapping(target = "playerId",  source = "playerId")
    @Mapping(target = "userId",    source = "userId")
    @Mapping(target = "matchId",   source = "marketId")    // marketId -> matchId
    @Mapping(target = "selection", source = "outcomeId")   // outcomeId -> selection
    @Mapping(target = "stake",     source = "stake")
    @Mapping(target = "odds",      source = "odds")
    BetPlacedEvent toLocalEvent(Bet bet);
}
