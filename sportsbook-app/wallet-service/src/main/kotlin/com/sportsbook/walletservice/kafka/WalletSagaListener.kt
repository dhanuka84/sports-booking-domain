package com.sportsbook.walletservice.kafka

import com.sportsbook.payment.contracts.saga.CreditWalletCommand
import com.sportsbook.payment.contracts.saga.WalletCreditedEvent
import com.sportsbook.walletservice.service.WalletService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WalletSagaListener(
    private val walletService: WalletService,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    private val eventsTopic = "deposit-events"

    @KafkaListener(
        topics = ["deposit-commands"],
        groupId = "wallet-saga-consumers",
    )
    fun onCommand(record: ConsumerRecord<String, Any>) {
        val value = record.value()
        if (value is CreditWalletCommand) {
            handleCreditWallet(value)
        }
    }

    private fun handleCreditWallet(cmd: CreditWalletCommand) {
        var success = true
        var failureReason: String? = null
        try {
            walletService.credit(cmd.playerId, cmd.amount)
        } catch (ex: Exception) {
            success = false
            failureReason = ex.message
        }

        val event = WalletCreditedEvent(
            cmd.sagaId,
            cmd.depositId,
            cmd.playerId,
            success,
            failureReason,
            Instant.now(),
        )
        kafkaTemplate.send(eventsTopic, cmd.playerId, event)
    }
}
