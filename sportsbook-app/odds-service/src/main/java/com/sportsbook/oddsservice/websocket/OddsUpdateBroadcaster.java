package com.sportsbook.oddsservice.websocket;

import com.sportsbook.bettingservice.kafka.BetPlacedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OddsUpdateBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public OddsUpdateBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastOdds(String matchId, Object oddsUpdate) {
        messagingTemplate.convertAndSend("/topic/odds/" + matchId, oddsUpdate);
    }
}
