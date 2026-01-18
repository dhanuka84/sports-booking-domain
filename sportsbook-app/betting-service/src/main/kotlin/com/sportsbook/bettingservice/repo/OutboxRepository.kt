package com.sportsbook.bettingservice.repo

import com.sportsbook.bettingservice.domain.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxRepository : JpaRepository<OutboxEvent, Long> {
    fun findTop50ByStatusOrderByCreatedAtAsc(status: String): List<OutboxEvent>
}
