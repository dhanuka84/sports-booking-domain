package com.sportsbook.bettingservice.kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BetPlacedEvent {
    private String id;
    private String playerId;
    private String matchId;
    private String userId;
    private String selection;
    private double stake;
    private double odds;

}