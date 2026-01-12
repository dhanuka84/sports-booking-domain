package com.sportsbook.bettingservice.outbox

import com.sportsbook.bettingservice.repo.OutboxRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxPublisher(
    private val repo: OutboxRepository,
    private val sender: OutboxSendPort,
) {

    @Scheduled(fixedDelay = 500)
    fun publish() {
        val batch = repo.findTop50ByStatusOrderByCreatedAtAsc("PENDING")
        for (event in batch) {
            try {
                val topic = event.topic ?: continue
                val key = event.key ?: continue
                val payload = event.payload ?: continue
                sender.send(topic, key, payload)
                event.status = "SENT"
                event.publishedAt = Instant.now()
                repo.save(event)
            } catch (ex: Exception) {
                // ignore and retry on next cycle
            }
        }
    }
}
