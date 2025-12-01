# Drop‑in use case (reuse existing classes)
This pack contains file replacements that implement one end‑to‑end flow:
Place Bet → Risk Decision → (later) Settlement trigger → Bet Settled.

Replace the files at identical paths in your repo with the ones here.

## Services touched
- betting-service
  - service/BetService.java  (emits bet.placed)
  - kafka/BetEventProducer.java
  - messaging/BetStatusListener.java (handles bet.accepted, bet.rejected, bet.settled)
- risk-service
  - kafka/BetPlacedListener.java (listens bet.placed, emits bet.accepted or bet.rejected)
- settlement-service
  - controller/SampleController.java (POST /api/results?betId=...&outcome=WIN|LOSE → emits bet.settled)

Each module also has an application.yml prefilled with dev ports and Avro settings.
Update ports or bootstrap URLs if your env differs.

## Topics
- bet.placed
- bet.accepted
- bet.rejected
- bet.settled

## Quick test
1) Start Kafka & Schema Registry (docker-compose or Testcontainers).
2) Start betting-service (8080), risk-service (8081), settlement-service (8082).
3) Place a bet:
   curl -X POST http://localhost:8080/api/bets -H 'Content-Type: application/json' -d '{"playerId":"p1","marketId":"m1","stakeCents":2500,"odds":1.9}'
4) Settle it:
   curl -X POST 'http://localhost:8082/api/results?betId=<BET_ID_FROM_STEP_3>&outcome=WIN'


## Build
mvn -pl payment-validator-service clean install
