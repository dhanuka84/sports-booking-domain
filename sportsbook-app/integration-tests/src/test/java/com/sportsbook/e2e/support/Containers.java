package com.sportsbook.e2e.support;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class Containers implements BeforeAllCallback {
  public static final Network NET = Network.newNetwork();

  public static final KafkaContainer KAFKA = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.7.1")
  ).withNetwork(NET);

  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:16-alpine")
  ).withUsername("test").withPassword("test").withDatabaseName("sports").withNetwork(NET);

  @Override public void beforeAll(ExtensionContext context) {
    if (!KAFKA.isRunning()) KAFKA.start();
    if (!POSTGRES.isRunning()) POSTGRES.start();
    System.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers());
    System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
    System.setProperty("spring.datasource.username", POSTGRES.getUsername());
    System.setProperty("spring.datasource.password", POSTGRES.getPassword());
  }
}
