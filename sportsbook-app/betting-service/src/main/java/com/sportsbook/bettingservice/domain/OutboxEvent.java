package com.sportsbook.bettingservice.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="outbox_events")
public class OutboxEvent { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String topic; private String key;
@Lob private byte[] payload; private String schemaSubject; private int schemaVersion; private String status; private Instant createdAt; private Instant publishedAt; }