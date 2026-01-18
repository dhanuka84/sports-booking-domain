package com.sportsbook.bettingservice.outbox

interface OutboxSendPort {
    fun send(topic: String, key: String, avroBinary: ByteArray)
}
