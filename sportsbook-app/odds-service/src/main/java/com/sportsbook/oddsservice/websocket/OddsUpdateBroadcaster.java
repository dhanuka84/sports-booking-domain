package com.sportsbook.oddsservice.websocket;

import com.sportsbook.events.OddsUpdated;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OddsUpdateBroadcaster {

    private final SimpMessagingTemplate ws;

    @KafkaListener(topics = "${app.kafka.topics.odds:odds.updates}", groupId = "odds-ws")
    public void onOdds(OddsUpdated evt) {
        // fan-out odds to websocket topic per event/market
        String dest = "/topic/odds/" + evt.getEventId() + "/" + evt.getMarketId();
        ws.convertAndSend(dest, evt);
    }
}
