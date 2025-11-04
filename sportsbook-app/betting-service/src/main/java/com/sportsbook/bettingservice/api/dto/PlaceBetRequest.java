package com.sportsbook.bettingservice.api.dto;

public record PlaceBetRequest(String playerId, String eventId, String userId, String marketId, String outcomeId, double stake,
                              double odds) {

}