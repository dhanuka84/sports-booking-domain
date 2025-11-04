package com.sportsbook.bettingservice.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "bets")
public class Bet {
    @Id
    private String id;
    private String playerId;
    private String userId;
    private String eventId;
    private String marketId;
    private String outcomeId;
    private double stake;
    private double odds;
    private double stakeCents;
    @Enumerated(EnumType.STRING)
    private BetStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}