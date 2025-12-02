### 

### **4.2. The Deposit Saga: Explained**

### **To understand the "Deposit Saga" in this sportsbook platform, we first need to define the architectural pattern it is based on and then map it to this specific implementation.**

### ---

### **1\. What is the Saga Pattern?**

### **In a monolithic application, you can use a single database transaction (ACID) to ensure that everything happens or nothing happens (e.g., *deduct money from bank* AND *add money to wallet*).**

### **In a Microservices Architecture, each service has its own database. You cannot have a single transaction that spans across the `Payment Validator` database and the `Wallet` database. If the Validator crashes after approving a deposit but before the Wallet is credited, you have data inconsistency (money is lost).**

### **The Saga Pattern solves this by breaking a large business process into a sequence of local transactions.**

* ### **Each service performs its own local transaction and publishes an event/message.**

* ### **That event triggers the next step in the next service.**

* ### **Orchestration: A central coordinator (the Orchestrator) tells participants what to do.**

* ### **Compensation: If a step fails, the saga executes "compensating transactions" to undo the changes made by previous steps.**

### ---

### **2\. The Deposit Saga Implementation**

### **In this codebase, the "Deposit" flow is implemented as an Orchestrated Saga. The Payment Validator Service acts as the central brain (Orchestrator) that decides whether to commit the transaction (credit wallet) or roll it back (reject/release reservation).**

#### **Step 1: Initiation (Ingest & Enrich)**

### **The saga begins when a user initiates a deposit.**

* ### **Module: `Payment Ingress Service`**

* ### **Action: Receives the raw HTTP request and pushes a `DepositInitiatedEvent` to the `deposit-ingest` topic.**

* ### **Module: `Risk Service`**

* ### **Action: Uses Kafka Streams to read the stream of deposits, calculate risk (e.g., velocity checks), and produce a `DepositEnrichedEvent` to the `deposit-enriched` topic.**

#### **Step 2: The Orchestrator (Decision Maker)**

### **This is the core of the Saga. The Orchestrator consumes the enriched event and makes the final decision.**

* ### **Module: `Payment Validator Service`**

* ### **Class: `ValidationOrchestrator`**

* ### **Logic:**

  1. ### **Idempotency Check: Checks its local DB (`DepositDecisionRepository`) to ensure this deposit hasn't already been processed.**

  2. ### **External Validation: Calls the `NordicApiAdapter` (Bank API) to verify funds. This is wrapped in a Circuit Breaker to handle external failures.**

  3. ### **Persist Decision: Saves the result (`APPROVED` or `REJECTED`) to its local PostgreSQL database. This is its Local Transaction.**

#### **Step 3: Saga Completion (Command Emission)**

### **Once the Orchestrator commits its decision, it emits a command to the next service. This is where the distributed nature is handled.**

* ### **Scenario A: Success (Happy Path)**

  * ### **Decision: `APPROVED`**

  * ### **Command: The Orchestrator sends a `CreditWalletCommand` to the `deposit-commands` topic.**

  * ### **Next Step: The Wallet Service picks this up.**

* ### **Scenario B: Failure (Compensation/Rejection)**

  * ### **Decision: `REJECTED` (e.g., Credit Card used, insufficient funds)**

  * ### **Command: The Orchestrator sends a `ReleaseReservationCommand`. This acts as a "Compensating Action" to tell the payment provider to release any hold on the user's funds.**

#### **Step 4: The Participant (Wallet Service)**

### **The Wallet Service is a "dumb" participant. It simply listens for commands and executes them.**

* ### **Module: `Wallet Service`**

* ### **Class: `WalletSagaListener`**

* ### **Action:**

  1. ### **Consumes `CreditWalletCommand`.**

  2. ### **Updates the user's balance in the `wallet` table (Local Transaction).**

  3. ### **Emits a `WalletCreditedEvent` (Success/Failure) back to the event stream.**

### **Summary of the Data Flow**

| Step | Service | Input | Logic | Output |
| :---- | :---- | :---- | :---- | :---- |
| **1** | **Ingest** | POST /deposit | Validate Request | DepositInitiatedEvent |
| **2** | **Risk** | DepositInitiatedEvent | Calculate Risk Flags | DepositEnrichedEvent |
| **3** | **Validator** | DepositEnrichedEvent | Call Bank API & Save DB | CreditWalletCommand OR ReleaseReservationCommand |
| **4** | **Wallet** | CreditWalletCommand | UPDATE wallet SET balance... | WalletCreditedEvent |

### 

### 

### **Kafka Circuit Breaker Implementation**

In a typical REST API, when a Circuit Breaker opens, we simply return a "Service Unavailable" error to the client. However, with **Kafka consumers**, simply throwing errors is bad practice because it forces the message into a retry loop or sends it to a Dead Letter Topic (DLT) prematurely.

Instead, this implementation uses a **Container Pausing Strategy**. When the external system (Bank API) goes down, the application **stops consuming** Kafka messages entirely until the system recovers.

Here is the breakdown of how this is implemented in the codebase:

#### **1\. protecting the External Call ( The Trigger)**

First, the actual external call to the Bank API is wrapped with Resilience4j's @CircuitBreaker annotation. This monitors the success/failure rate of that specific method.

* **File:** NordicApiAdapter.java  
* **Logic:** The checkSource method is tagged with bankApiBreaker. If this method throws exceptions (like connection timeouts), Resilience4j records them.

Java

@Override  
@CircuitBreaker(name \= "bankApiBreaker") // \<--- The Monitor  
public FundingSourceType checkSource(DepositEnrichedEvent depositEnriched) {  
    return webClient.post()  
            // ... (HTTP Call)  
            .block(Duration.ofSeconds(2));  
}

#### **2\. connecting Circuit State to Kafka (The Glue)**

This is the most critical part of this implementation. You have a configuration class that listens for state changes in the Circuit Breaker and physically pauses or resumes the Kafka Listener Container.

* **File:** KafkaCircuitBreakerConfig.java  
* **Logic:**  
  * It retrieves the validatorListener container from the Spring registry.  
  * **On OPEN (Failure):** It calls container.pause(). This stops the consumer from fetching new records from the broker.  
  * **On CLOSED/HALF-OPEN (Recovery):** It calls container.resume(). Consumption restarts.

Java

private void handleStateTransition(CircuitBreakerOnStateTransitionEvent event) {  
    // 1\. Get the specific Kafka listener  
    MessageListenerContainer container \= registry.getListenerContainer("validatorListener");

    // 2\. Pause or Resume based on Circuit Breaker state  
    switch (event.getStateTransition()) {  
        case CLOSED\_TO\_OPEN, HALF\_OPEN\_TO\_OPEN \-\> container.pause(); // Stop consuming  
        case OPEN\_TO\_HALF\_OPEN, HALF\_OPEN\_TO\_CLOSED \-\> container.resume(); // Start consuming  
    }  
}

#### **3\. identifying the Listener**

To make the "glue" code above work, the Kafka listener must have a specific ID that matches the one looked up in the registry ("validatorListener").

* **File:** DepositValidatorListener.java  
* **Logic:** The id attribute is explicitly set.

Java

@KafkaListener(  
        id \= "validatorListener", // \<--- Must match the Config lookup  
        topics \= "${psv.topics.deposit-enriched}",  
        // ...  
)  
public void onDeposit(...) { ... }

#### **4\. Configuration Rules**

The sensitivity of the breaker is defined in the YAML configuration.

* **File:** application.yml (payment-validator-service)  
* **Logic:**  
  * slidingWindowSize: 5: It looks at the last 5 calls.  
  * failureRateThreshold: 50: If 50% (2.5 calls) fail, the circuit opens.  
  * waitDurationInOpenState: 10s: It pauses for 10 seconds before trying again (entering HALF-OPEN).

### **Summary of the Workflow**

1. **Healthy State:** The listener consumes messages \-\> Calls NordicApiAdapter.  
2. **Failure:** The Bank API goes down. NordicApiAdapter throws exceptions.  
3. **Circuit Opens:** After 5 failed attempts (configured in YAML), Resilience4j changes state to OPEN.  
4. **Pause:** KafkaCircuitBreakerConfig detects the change and calls validatorListener.pause().  
   * *Result:* The application stops processing the deposit-enriched topic. The offset is not committed, so no messages are lost. They pile up in the Kafka broker (Server-side).  
5. **Recovery:** After 10 seconds, Resilience4j changes state to HALF-OPEN.  
6. **Resume:** The config detects this and calls validatorListener.resume(). The app processes one "test" message. If successful, it fully resumes.