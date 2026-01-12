package com.sportsbook.bettingservice.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "outbox_events")
class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    var topic: String? = null
    var key: String? = null

    @Lob
    var payload: ByteArray? = null
    var schemaSubject: String? = null
    var schemaVersion: Int = 0
    var status: String? = null
    var createdAt: Instant? = null
    var publishedAt: Instant? = null
}
