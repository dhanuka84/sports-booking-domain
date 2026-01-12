package com.sportsbook.playerservice.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SampleEntityTest {

    @Test
    fun testSampleEntityFields() {
        val entity = SampleEntity()
        entity.id = "user123"
        entity.name = "Alice"

        assertEquals("user123", entity.id)
        assertEquals("Alice", entity.name)
    }
}
