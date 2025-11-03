package com.sportsbook.playerservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleEntityTest {

    @Test
    void testSampleEntityFields() {
        SampleEntity entity = new SampleEntity();
        entity.setId("user123");
        entity.setName("Alice");

        assertEquals("user123", entity.getId());
        assertEquals("Alice", entity.getName());
    }
}
