package com.sportsbook.oddsservice.websocket

import com.sportsbook.events.OddsUpdated
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class OddsUpdateBroadcaster(
    private val ws: SimpMessagingTemplate,
) {

    @KafkaListener(topics = ["\${app.kafka.topics.odds:odds.updates}"], groupId = "odds-ws")
    fun onOdds(evt: OddsUpdated) {
        val dest = "/topic/odds/${evt.eventId}/${evt.marketId}"
        ws.convertAndSend(dest, evt)
    }
}
