package com.sportsbook.e2e.support

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class Containers : BeforeAllCallback {

    companion object {
        val NET: Network = Network.newNetwork()

        val KAFKA: KafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.7.1"),
        ).withNetwork(NET)

        val POSTGRES: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>(
            DockerImageName.parse("postgres:16-alpine"),
        ).apply {
            withUsername("test")
            withPassword("test")
            withDatabaseName("sports")
            withNetwork(NET)
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        if (!KAFKA.isRunning) KAFKA.start()
        if (!POSTGRES.isRunning) POSTGRES.start()
        System.setProperty("spring.kafka.bootstrap-servers", KAFKA.bootstrapServers)
        System.setProperty("spring.datasource.url", POSTGRES.jdbcUrl)
        System.setProperty("spring.datasource.username", POSTGRES.username)
        System.setProperty("spring.datasource.password", POSTGRES.password)
    }
}
