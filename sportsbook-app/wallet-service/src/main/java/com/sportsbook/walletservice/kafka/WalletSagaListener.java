package com.sportsbook.walletservice.kafka;

import com.sportsbook.payment.contracts.saga.CreditWalletCommand;
import com.sportsbook.payment.contracts.saga.WalletCreditedEvent;
import com.sportsbook.walletservice.service.WalletService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WalletSagaListener {

    private final WalletService walletService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String eventsTopic = "deposit-events";

    public WalletSagaListener(WalletService walletService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.walletService = walletService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "deposit-commands",
            groupId = "wallet-saga-consumers"
    )
    public void onCommand(ConsumerRecord<String, Object> record) {
        Object value = record.value();
        if (value instanceof CreditWalletCommand cmd) {
            handleCreditWallet(cmd);
        }
    }

    private void handleCreditWallet(CreditWalletCommand cmd) {
        boolean success = true;
        String failureReason = null;
        try {
            walletService.credit(cmd.playerId(), cmd.amount());
        } catch (Exception e) {
            success = false;
            failureReason = e.getMessage();
        }

        var event = new WalletCreditedEvent(
                cmd.sagaId(),
                cmd.depositId(),
                cmd.playerId(),
                success,
                failureReason,
                Instant.now()
        );
        kafkaTemplate.send(eventsTopic, cmd.playerId(), event);
    }
}
