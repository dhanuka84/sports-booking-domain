package com.sportsbook.bettingservice.messaging

import com.sportsbook.bettingservice.domain.BetStatus
import com.sportsbook.bettingservice.repo.BetRepository
import com.sportsbook.events.BetAccepted
import com.sportsbook.events.BetRejected
import com.sportsbook.events.BetSettled
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BetStatusListener(
    private val betRepository: BetRepository,
) {

    @KafkaListener(topics = ["bet.accepted"], groupId = "betting-service")
    @Transactional
    fun onAccepted(evt: BetAccepted) {
        betRepository.findById(evt.betId)
            .ifPresent { bet ->
                if (bet.status == BetStatus.PENDING) {
                    bet.status = BetStatus.ACCEPTED
                    betRepository.save(bet)
                }
            }
    }

    @KafkaListener(topics = ["bet.rejected"], groupId = "betting-service")
    @Transactional
    fun onRejected(evt: BetRejected) {
        betRepository.findById(evt.betId)
            .ifPresent { bet ->
                bet.status = BetStatus.REJECTED
                betRepository.save(bet)
            }
    }

    @KafkaListener(topics = ["bet.settled"], groupId = "betting-service")
    @Transactional
    fun onSettled(evt: BetSettled) {
        betRepository.findById(evt.betId)
            .ifPresent { bet ->
                bet.status = BetStatus.SETTLED
                betRepository.save(bet)
            }
    }
}
