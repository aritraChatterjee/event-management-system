# DDD Visual Diagrams - Alf.io

## 1. Bounded Context Diagram

```mermaid
graph TB
    subgraph "Core Domain"
        EM[Event Management Context]
        RT[Reservation & Ticketing Context]
        SC[Subscription Context]
    end
    
    subgraph "Supporting Domains"
        BC[Billing Context]
        PC[Payment Context]
        NC[Notification Context]
    end
    
    subgraph "Generic Domains"
        OC[Organization & User Context]
        AC[Audit Context]
    end
    
    OC -->|provides user auth| EM
    OC -->|provides user auth| SC
    EM -->|creates tickets for| RT
    SC -->|alternative to| EM
    RT -->|processes payment via| PC
    RT -->|generates| BC
    BC -->|sends via| NC
    RT -->|sends confirmation via| NC
    EM -->|logs to| AC
    RT -->|logs to| AC
    SC -->|logs to| AC
```

## 2. Event Management Aggregate

```mermaid
classDiagram
    class Event {
        <<Aggregate Root>>
        +int id
        +String shortName
        +String displayName
        +EventFormat format
        +Status status
        +ZonedDateTime begin
        +ZonedDateTime end
        +String currency
        +List~PaymentProxy~ allowedPaymentProxies
        +validateDateRange()
        +isOnline() boolean
        +isExpired() boolean
    }
    
    class TicketCategory {
        <<Entity>>
        +int id
        +String name
        +int maxTickets
        +boolean bounded
        +ZonedDateTime inception
        +ZonedDateTime expiration
        +isSaleable() boolean
    }
    
    class AdditionalService {
        <<Entity>>
        +int id
        +String name
        +AdditionalServiceType type
        +int availableItems
        +BigDecimal price
        +isAvailable() boolean
    }
    
    class EventDescription {
        <<Entity>>
        +int eventId
        +String locale
        +String description
    }
    
    class EventFormat {
        <<Value Object>>
        IN_PERSON
        ONLINE
        HYBRID
    }
    
    class Status {
        <<Value Object>>
        DRAFT
        PUBLIC
        DISABLED
    }
    
    Event "1" *-- "many" TicketCategory
    Event "1" *-- "many" AdditionalService
    Event "1" *-- "many" EventDescription
    Event --> EventFormat
    Event --> Status
```

## 3. Reservation Aggregate

```mermaid
classDiagram
    class TicketReservation {
        <<Aggregate Root>>
        +String id
        +TicketReservationStatus status
        +Date validity
        +String fullName
        +String email
        +int totalPrice
        +String currency
        +updateStatus(status)
        +isExpired() boolean
        +isComplete() boolean
    }
    
    class Ticket {
        <<Entity>>
        +String uuid
        +int categoryId
        +TicketStatus status
        +String fullName
        +String email
        +boolean lockedAssignment
        +checkIn()
        +release()
        +assignTo(name, email)
    }
    
    class AdditionalServiceItem {
        <<Entity>>
        +int id
        +int additionalServiceId
        +int quantity
        +BigDecimal price
    }
    
    class TicketReservationStatus {
        <<Value Object>>
        PENDING
        IN_PAYMENT
        COMPLETE
        CANCELLED
        STUCK
    }
    
    class TicketStatus {
        <<Value Object>>
        FREE
        PENDING
        ACQUIRED
        TO_BE_PAID
        CHECKED_IN
        RELEASED
        CANCELLED
    }
    
    class BillingDetails {
        <<Value Object>>
        +String companyName
        +String addressLine1
        +String city
        +String taxId
    }
    
    TicketReservation "1" *-- "many" Ticket
    TicketReservation "1" *-- "many" AdditionalServiceItem
    TicketReservation "1" *-- "1" BillingDetails
    TicketReservation --> TicketReservationStatus
    Ticket --> TicketStatus
```

## 4. Subscription Aggregate

```mermaid
classDiagram
    class SubscriptionDescriptor {
        <<Aggregate Root>>
        +UUID id
        +String title
        +int maxAvailable
        +BigDecimal price
        +SubscriptionStatus status
        +ZonedDateTime onSaleFrom
        +ZonedDateTime onSaleTo
        +SubscriptionValidityType validityType
        +SubscriptionUsageType usageType
        +isActive() boolean
        +isOnSale() boolean
    }
    
    class Subscription {
        <<Entity>>
        +UUID id
        +UUID subscriptionDescriptorId
        +String reservationId
        +int maxUsage
        +ZonedDateTime validFrom
        +ZonedDateTime validTo
        +SubscriptionStatus status
        +consume()
        +isValid() boolean
    }
    
    class EventSubscriptionLink {
        <<Entity>>
        +int eventId
        +UUID subscriptionDescriptorId
        +int pricePerTicket
        +String compatibleCategories
    }
    
    class SubscriptionStatus {
        <<Value Object>>
        ACTIVE
        NOT_ACTIVE
    }
    
    class SubscriptionValidityType {
        <<Value Object>>
        STANDARD
        CUSTOM
        NOT_SET
    }
    
    class SubscriptionUsageType {
        <<Value Object>>
        ONCE_PER_EVENT
        UNLIMITED
    }
    
    SubscriptionDescriptor "1" *-- "many" Subscription
    SubscriptionDescriptor "1" *-- "many" EventSubscriptionLink
    SubscriptionDescriptor --> SubscriptionStatus
    SubscriptionDescriptor --> SubscriptionValidityType
    SubscriptionDescriptor --> SubscriptionUsageType
```

## 5. Organization Aggregate

```mermaid
classDiagram
    class Organization {
        <<Aggregate Root>>
        +int id
        +String name
        +String email
        +String slug
        +String description
        +validate()
    }
    
    class User {
        <<Entity>>
        +int id
        +String username
        +String password
        +String firstName
        +String lastName
        +UserType userType
        +isEnabled() boolean
    }
    
    class Group {
        <<Entity>>
        +int id
        +String name
        +String description
        +int organizationId
    }
    
    class Authority {
        <<Value Object>>
        +String username
        +String role
    }
    
    Organization "1" *-- "many" User
    Organization "1" *-- "many" Group
    User "1" *-- "many" Authority
```

## 6. Transaction Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant ReservationMgr
    participant PaymentMgr
    participant BillingMgr
    participant NotificationMgr
    
    User->>Controller: Create Reservation
    Controller->>ReservationMgr: createTicketReservation()
    ReservationMgr->>ReservationMgr: validateAvailability()
    ReservationMgr->>ReservationMgr: applyPromoCode()
    ReservationMgr->>ReservationMgr: calculatePrice()
    ReservationMgr-->>Controller: reservationId
    Controller-->>User: Reservation Created
    
    User->>Controller: Submit Payment
    Controller->>PaymentMgr: processPayment()
    PaymentMgr->>PaymentMgr: validatePaymentMethod()
    PaymentMgr->>PaymentMgr: processWithGateway()
    PaymentMgr-->>Controller: Payment Success
    
    Controller->>ReservationMgr: confirmReservation()
    ReservationMgr->>BillingMgr: generateBillingDocument()
    BillingMgr-->>ReservationMgr: Invoice Created
    ReservationMgr->>NotificationMgr: sendConfirmationEmail()
    NotificationMgr-->>ReservationMgr: Email Sent
    ReservationMgr-->>Controller: Confirmed
    Controller-->>User: Confirmation & Invoice
```

## 7. Reservation State Machine

```mermaid
stateDiagram-v2
    [*] --> PENDING: Create Reservation
    PENDING --> IN_PAYMENT: Start Payment
    IN_PAYMENT --> EXTERNAL_PROCESSING_PAYMENT: External Gateway
    IN_PAYMENT --> OFFLINE_PAYMENT: Offline Method
    IN_PAYMENT --> COMPLETE: Payment Success
    IN_PAYMENT --> PENDING: Payment Failed
    EXTERNAL_PROCESSING_PAYMENT --> COMPLETE: Payment Confirmed
    EXTERNAL_PROCESSING_PAYMENT --> CANCELLED: Payment Failed
    OFFLINE_PAYMENT --> COMPLETE: Payment Verified
    OFFLINE_PAYMENT --> CANCELLED: Not Paid
    PENDING --> STUCK: System Error
    PENDING --> CANCELLED: User Cancels
    PENDING --> CANCELLED: Expired
    COMPLETE --> CREDIT_NOTE_ISSUED: Refund
    STUCK --> CANCELLED: Admin Cancels
    COMPLETE --> [*]
    CANCELLED --> [*]
    CREDIT_NOTE_ISSUED --> [*]
```

## 8. Ticket State Machine

```mermaid
stateDiagram-v2
    [*] --> FREE: Create Ticket
    FREE --> PENDING: Reserve
    PENDING --> ACQUIRED: Confirm Payment
    PENDING --> TO_BE_PAID: Offline Payment
    TO_BE_PAID --> ACQUIRED: Payment Confirmed
    ACQUIRED --> CHECKED_IN: Check In
    PENDING --> RELEASED: Release
    PENDING --> CANCELLED: Cancel
    ACQUIRED --> CANCELLED: Cancel & Refund
    RELEASED --> FREE: Return to Pool
    CHECKED_IN --> [*]
    CANCELLED --> [*]
```

## 9. Event Lifecycle

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Create Event
    DRAFT --> PUBLIC: Publish
    PUBLIC --> DISABLED: Disable Sales
    DISABLED --> PUBLIC: Re-enable
    PUBLIC --> [*]: Event Ends
    DRAFT --> [*]: Delete (Admin)
```

## 10. Payment Processing Flow

```mermaid
flowchart TD
    A[Start Payment] --> B{Payment Method?}
    B -->|Credit Card| C[PaymentManager]
    B -->|PayPal| C
    B -->|Bank Transfer| D[Offline Payment]
    B -->|On-Site| D
    
    C --> E{Gateway Processing}
    E -->|Success| F[Update Transaction: COMPLETE]
    E -->|Failed| G[Update Transaction: FAILED]
    
    D --> H[Mark: OFFLINE_PAYMENT]
    H --> I[Admin Verification]
    I -->|Verified| F
    I -->|Rejected| G
    
    F --> J[Update Reservation: COMPLETE]
    G --> K[Update Reservation: PENDING]
    
    J --> L[Generate Billing Document]
    L --> M[Send Confirmation Email]
    M --> N[End: Success]
    
    K --> O[Notify User]
    O --> P[End: Failed]
```

## 11. Check-In Process

```mermaid
flowchart TD
    A[Scan Ticket QR] --> B{Ticket Valid?}
    B -->|No| C[Show Error]
    C --> Z[End]
    
    B -->|Yes| D{Already Checked In?}
    D -->|Yes| E[Show Warning: Already Checked In]
    E --> F{Force Check-In?}
    F -->|No| Z
    F -->|Yes| G[Update Check-In Time]
    
    D -->|No| H{Ticket Status?}
    H -->|Not ACQUIRED| I[Error: Invalid Status]
    I --> Z
    
    H -->|ACQUIRED| J[Update Status: CHECKED_IN]
    J --> K[Record Check-In Time]
    K --> L[Log Audit Event]
    L --> M[Show Success]
    M --> Z
```

## 12. Promo Code Application

```mermaid
flowchart TD
    A[Apply Promo Code] --> B{Code Exists?}
    B -->|No| C[Error: Invalid Code]
    C --> Z[End]
    
    B -->|Yes| D{Valid Date Range?}
    D -->|No| E[Error: Expired/Not Started]
    E --> Z
    
    D -->|Yes| F{Usage Limit?}
    F -->|Exceeded| G[Error: Code Fully Used]
    G --> Z
    
    F -->|Available| H{Category Restriction?}
    H -->|Yes| I{Ticket in Valid Category?}
    I -->|No| J[Error: Invalid Category]
    J --> Z
    
    I -->|Yes| K[Calculate Discount]
    H -->|No| K
    
    K --> L{Discount Type?}
    L -->|FIXED_AMOUNT| M[Reduce by Fixed Amount]
    L -->|PERCENTAGE| N[Reduce by Percentage]
    L -->|ACCESS| O[Grant Access to Hidden Category]
    
    M --> P[Apply Discount]
    N --> P
    O --> P
    
    P --> Q[Update Reservation Price]
    Q --> R[Increment Usage Count]
    R --> S[End: Success]
```

## 13. Extension System Architecture

```mermaid
flowchart LR
    subgraph "Core Domain"
        A[Domain Event]
    end
    
    subgraph "Extension Manager"
        B[Event Dispatcher]
        C[Script Engine<br/>Rhino JS]
    end
    
    subgraph "Extensions"
        D[Reservation Hook]
        E[Ticket Hook]
        F[Email Hook]
        G[Custom Logic]
    end
    
    A -->|trigger| B
    B -->|execute| C
    C -->|run| D
    C -->|run| E
    C -->|run| F
    C -->|run| G
    
    D -->|modify| H[Reservation Data]
    E -->|modify| I[Ticket Data]
    F -->|modify| J[Email Content]
```

## 14. Database Schema Overview

```mermaid
erDiagram
    ORGANIZATION ||--o{ EVENT : owns
    ORGANIZATION ||--o{ SUBSCRIPTION_DESCRIPTOR : owns
    ORGANIZATION ||--o{ BA_USER : has
    
    EVENT ||--o{ TICKET_CATEGORY : contains
    EVENT ||--o{ ADDITIONAL_SERVICE : offers
    EVENT ||--o{ TICKETS_RESERVATION : receives
    
    TICKETS_RESERVATION ||--o{ TICKET : contains
    TICKETS_RESERVATION ||--o{ ADDITIONAL_SERVICE_ITEM : includes
    TICKETS_RESERVATION ||--o{ B_TRANSACTION : processes
    TICKETS_RESERVATION ||--o{ BILLING_DOCUMENT : generates
    
    TICKET_CATEGORY ||--o{ TICKET : categorizes
    
    SUBSCRIPTION_DESCRIPTOR ||--o{ SUBSCRIPTION : instances
    SUBSCRIPTION_DESCRIPTOR ||--o{ EVENT_SUBSCRIPTION_LINK : links
    
    EVENT ||--o{ PROMO_CODE : offers
    
    BA_USER ||--o{ AUTHORITY : has
    BA_USER }o--o{ ORGANIZATION : member_of
```

## 15. Layered Architecture

```mermaid
flowchart TD
    subgraph "Presentation Layer"
        A[REST Controllers]
        B[Web Controllers]
        C[Admin UI]
        D[Public UI]
    end
    
    subgraph "Application Layer"
        E[EventManager]
        F[TicketReservationManager]
        G[SubscriptionManager]
        H[PaymentManager]
        I[BillingDocumentManager]
    end
    
    subgraph "Domain Layer"
        J[Event Aggregate]
        K[Reservation Aggregate]
        L[Subscription Aggregate]
        M[Organization Aggregate]
        N[Value Objects]
    end
    
    subgraph "Infrastructure Layer"
        O[Repositories<br/>NPJT]
        P[PostgreSQL]
        Q[Email Service]
        R[Payment Gateways]
    end
    
    A --> E
    B --> E
    A --> F
    B --> F
    C --> G
    
    E --> J
    F --> K
    G --> L
    H --> K
    
    J --> O
    K --> O
    L --> O
    M --> O
    
    O --> P
    E --> Q
    H --> R
```

---

**Diagrams Version**: 1.0  
**Last Updated**: February 12, 2026  
**Format**: Mermaid.js (compatible with GitHub, GitLab, Obsidian, etc.)

