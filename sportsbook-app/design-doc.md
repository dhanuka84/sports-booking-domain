Here is the design document for the Sportsbook Platform based on the provided source code and domain documentation.

Design Document: Sportsbook Platform
1. Executive Summary
   The Sportsbook Platform is a distributed, event-driven microservices system designed to handle the core lifecycle of sports betting: wagering, risk assessment, settlement, and financial transaction management. The system utilizes Spring Boot for service implementation and Apache Kafka as the central nervous system for asynchronous communication, ensuring high throughput and loose coupling between domains.

2. System Architecture
   The platform follows a Domain-Driven Design (DDD) approach, where each microservice corresponds to a specific bounded context.

Core Technology Stack
Language: Java 21

Framework: Spring Boot 3.3.4

Messaging: Apache Kafka (with Confluent Avro Serializers)

Database: PostgreSQL (implied by drivers) and H2 (for testing)

Protocol: REST (Sync), Kafka (Async), WebSockets (Real-time updates), gRPC (Proto definitions exist)

Frontend: React 18

3. Module Design Specifications
   3.1. Betting Service
   Role: The core engine responsible for receiving bet requests, persisting the initial state, and managing the lifecycle of a wager.

Responsibilities:

Bet Placement: Exposes a REST API (POST /api/bets) to accept wager requests.

Persistence: Saves bets to the database with a PENDING status.

Event Publishing: Uses the Transactional Outbox Pattern to ensure data consistency. It saves an OutboxEvent in the same transaction as the Bet entity, which is then picked up by a scheduler (OutboxPublisher) and pushed to Kafka.

State Updates: Listens for risk and settlement events to update bet status to ACCEPTED, REJECTED, or SETTLED.

Key Interfaces:

BetController: REST endpoint for clients.

BetEventProducer: Wrapper for Kafka template interactions.

BetStatusListener: Kafka listener for updating local state based on downstream decisions.

3.2. Risk Service
Role: The guardian of profitability, responsible for analyzing bets and financial transactions to mitigate exposure.

Responsibilities:

Bet Risk: Subscribes to bet.placed. It evaluates the stake (e.g., checks if stake > limit) and emits either BetAccepted or BetRejected.

Deposit Risk: Utilizes Kafka Streams (DepositRiskTopology) to analyze the stream of deposit requests (deposit-ingest). It enriches events with risk flags before passing them to the payment validator.

Key Interfaces:

BetPlacedListener: Evaluates individual bet rules.

DepositRiskTopology: Stream processing topology for financial risk.

3.3. Odds Service
Role: Manages the pricing data (odds) for sporting events and distributes them in real-time.

Responsibilities:

Data Ingestion: Receives OddsUpdated events (likely from external feed providers).

Real-time Broadcasting: Uses a OddsUpdateBroadcaster to push updates to frontend clients via WebSockets (STOMP protocol).

API: Provides REST endpoints to fetch available markets.

Key Interfaces:

WebSocketConfig: Configures the STOMP broker.

OddsServiceProto: gRPC definition for high-performance internal odds fetching.

3.4. Wallet Service
Role: Manages the financial ledger for players, handling deposits and settlement payouts.

Responsibilities:

Credit/Debit: Manages the Wallet entity, ensuring atomic updates to user balances.

Settlement Processing: Listens to bet.settled. If the result is WIN, it calculates the payout and credits the user's wallet.

Saga Participation: Listens for CreditWalletCommand from the Payment Validator service to finalize deposits.

Key Interfaces:

WalletService: Transactional logic for balance updates.

WalletSagaListener: Handles commands from the orchestration layer.

3.5. Settlement Service
Role: Determines the outcome of events and triggers the resolution of bets.

Responsibilities:

Result Publishing: Exposes an API (POST /api/results) to manually trigger results (for testing/admin).

Event Emission: Publishes BetSettled events containing the result (WIN/LOSE/VOID) and payout calculations.

Key Interfaces:

SampleController: Acts as the trigger point for settlement logic.

3.6. Payment Domain (Ingress & Validator)
Role: Handles the complex flow of depositing funds using the Saga Pattern to ensure distributed transaction consistency.

Modules:

Payment Ingress Service: The entry point. It accepts raw deposit requests and pushes them to a deposit-ingest Kafka topic.

Payment Validator Service: The Saga Orchestrator.

Validation: Checks external credit providers (via NordicApiAdapter).

Resilience: Implements Resilience4j Circuit Breakers to handle external API failures gracefully.

Orchestration: Emits commands (CreditWalletCommand or ReleaseReservationCommand) based on validation results.

Idempotency: Checks DepositDecisionRepository to prevent processing the same deposit twice.

4. Key Workflows
   4.1. The Bet Lifecycle
   Placement: User submits bet -> Betting Service persists as PENDING -> Writes to Outbox -> Publishes bet.placed to Kafka.

Risk Check: Risk Service consumes bet.placed -> Evaluates stake logic -> Publishes bet.accepted (or rejected).

Confirmation: Betting Service consumes bet.accepted -> Updates DB status to ACCEPTED.

Settlement: Event concludes -> Settlement Service publishes bet.settled -> Betting Service marks as SETTLED -> Wallet Service credits funds if won.

4.2. The Deposit Saga
Ingest: Payment Ingress receives request -> Publishes deposit-ingest.

Risk Analysis: Risk Service (Streams) reads deposit-ingest -> Calculates risk -> Publishes deposit-enriched.

Validation: Payment Validator reads deposit-enriched -> Calls external Bank API (with Circuit Breaker) -> Persists Decision.

Finalization:

If Approved: Validator publishes CreditWalletCommand -> Wallet Service updates balance.

If Rejected: Validator publishes ReleaseReservationCommand.

5. Deployment & Infrastructure
   The application is designed to be containerized using Docker. A docker-compose.yml is provided to orchestrate the environment, including:

Zookeeper & Kafka: Message broker.

Schema Registry: For managing Avro schemas (BetPlaced, OddsUpdated, etc.) to ensure contract compatibility between services.

PostgreSQL: Relational database for consistency.