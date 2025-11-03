package com.sportsbook.bettingservice.outbox;

import com.sportsbook.bettingservice.repo.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository repo;
    private final OutboxSendPort sender;

    @Scheduled(fixedDelay = 500)
    public void publish() {
        var batch = repo.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        for (var e : batch) {
            try {
                sender.send(e.getTopic(), e.getKey(), e.getPayload());
                e.setStatus("SENT");
                e.setPublishedAt(Instant.now());
                repo.save(e);
            } catch (Exception ex) {
            }
        }
    }
}