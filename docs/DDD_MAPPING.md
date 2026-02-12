# Domain-Driven Design (DDD) Mapping - Alf.io Event Management System

## Executive Summary

This document provides a comprehensive Domain-Driven Design mapping for the Alf.io event management system. The system is organized around several bounded contexts centered on event ticketing, subscriptions, billing, and user management.

---

## 1. Bounded Contexts

### 1.1 Event Management Context
**Responsibility**: Managing events, ticket categories, and event-related configurations

**Core Domain**: Yes

**Key Aggregates**:
- Event
- TicketCategory
- AdditionalService

### 1.2 Reservation & Ticketing Context
**Responsibility**: Handling ticket reservations, purchases, and ticket lifecycle

**Core Domain**: Yes

**Key Aggregates**:
- TicketReservation
- Ticket

### 1.3 Subscription Context
**Responsibility**: Managing recurring subscriptions and their lifecycle

**Core Domain**: Yes

**Key Aggregates**:
- SubscriptionDescriptor
- Subscription

### 1.4 Billing Context
**Responsibility**: Invoice and billing document generation

**Core Domain**: No (Supporting)

**Key Aggregates**:
- BillingDocument

### 1.5 Organization & User Context
**Responsibility**: Managing organizations, users, authorities, and access control

**Core Domain**: No (Generic)

**Key Aggregates**:
- Organization
- User
- Group

### 1.6 Payment Context
**Responsibility**: Processing payments and managing transactions

**Core Domain**: No (Supporting)

**Key Aggregates**:
- Transaction

### 1.7 Notification Context
**Responsibility**: Email notifications and communication

**Core Domain**: No (Supporting)

---

## 2. Aggregates

### 2.1 Event Aggregate

**Aggregate Root**: `Event`

**Entities**:
- `Event` - Root entity representing an event
- `TicketCategory` - Categories/tiers of tickets
- `AdditionalService` - Extra services offered with tickets
- `EventDescription` - Localized event descriptions
- `TicketCategoryDescription` - Localized category descriptions

**Value Objects**:
- `EventFormat` (enum: IN_PERSON, ONLINE, HYBRID)
- `EventStatus` (enum: DRAFT, PUBLIC, DISABLED)
- `PriceContainer.VatStatus`
- `PaymentProxy` list
- `AlfioMetadata`

**Repository**: `EventRepository`

**Domain Services**:
- `EventManager` - Core event management operations
- `EventStatisticsManager` - Event statistics and analytics
- `TicketCategoryAvailabilityManager` - Ticket availability calculations

**Invariants**:
- Event must have valid start and end dates (end > start)
- Event must belong to an organization
- Available seats must be >= 0
- Currency and VAT settings must be valid
- At least one payment proxy must be configured

---

### 2.2 TicketReservation Aggregate

**Aggregate Root**: `TicketReservation`

**Entities**:
- `TicketReservation` - Root entity for a reservation
- `Ticket` - Individual tickets within the reservation
- `AdditionalServiceItem` - Additional service items in reservation

**Value Objects**:
- `TicketReservationStatus` (enum: PENDING, IN_PAYMENT, EXTERNAL_PROCESSING_PAYMENT, OFFLINE_PAYMENT, DEFERRED_OFFLINE_PAYMENT, COMPLETE, STUCK, CANCELLED, CREDIT_NOTE_ISSUED)
- `BillingDetails`
- `TicketReservationInvoicingAdditionalInfo`
- `OrderSummary`
- `CustomerName`

**Repository**: `TicketReservationRepository`, `TicketRepository`

**Domain Services**:
- `TicketReservationManager` - Main orchestrator for reservations
- `AdminReservationManager` - Administrative reservation operations
- `ReservationFinalizer` - Finalizing payment and confirmation

**Invariants**:
- Reservation must have an expiration date
- Tickets in reservation must belong to the same event
- Total price must match sum of ticket + additional service prices
- Reservation status transitions must follow defined workflow
- Completed reservations cannot be modified

**Domain Events**:
- `ReservationCreated`
- `ReservationConfirmed`
- `ReservationCancelled`
- `TicketAssigned`

---

### 2.3 Ticket Entity

**Note**: While part of TicketReservation aggregate, Ticket has significant behavior

**Attributes**:
- `uuid` - Unique identifier
- `status` - TicketStatus (FREE, PENDING, ACQUIRED, TO_BE_PAID, CHECKED_IN, etc.)
- `categoryId` - Reference to TicketCategory
- `fullName`, `firstName`, `lastName`, `email`
- `lockedAssignment` - Whether assignment can be changed
- `userLanguage`
- `metadata`

**Repository**: `TicketRepository`

**Domain Services**:
- `AttendeeManager` - Managing ticket attendee information
- `CheckInManager` - Check-in operations

**Invariants**:
- Ticket must belong to a reservation (or be FREE)
- Checked-in tickets cannot be released
- Ticket category must be valid for the event

---

### 2.4 SubscriptionDescriptor Aggregate

**Aggregate Root**: `SubscriptionDescriptor`

**Entities**:
- `SubscriptionDescriptor` - Root entity defining subscription plans
- `Subscription` - Individual subscription instances
- `EventSubscriptionLink` - Links subscriptions to events

**Value Objects**:
- `SubscriptionStatus` (enum: ACTIVE, NOT_ACTIVE)
- `SubscriptionValidityType` (enum: STANDARD, CUSTOM, NOT_SET)
- `SubscriptionTimeUnit` (enum: DAYS, MONTHS, YEARS)
- `SubscriptionUsageType` (enum: ONCE_PER_EVENT, UNLIMITED)
- `SubscriptionPriceContainer`

**Repository**: `SubscriptionRepository`

**Domain Services**:
- `SubscriptionManager` - Subscription descriptor management
- `SubscriptionReservationManager` - Subscription purchase and reservation

**Invariants**:
- Subscription must belong to an organization
- Validity period must be consistent (from < to)
- Max available must be >= 0
- Price must be >= 0
- On-sale period must be valid

---

### 2.5 Organization Aggregate

**Aggregate Root**: `Organization`

**Entities**:
- `Organization` - Root entity
- `User` - Users within organization
- `Group` - User groups

**Value Objects**:
- `OrganizationContact`
- `Authority` (roles and permissions)

**Repository**: `OrganizationRepository`, `UserRepository`, `GroupRepository`

**Domain Services**:
- `OrganizationDeleter` - Safe organization deletion
- `AccessService` - Authorization and access control
- `GroupManager` - Group management

**Invariants**:
- Organization must have unique name and slug
- Organization must have valid contact email
- Each user must have at least one authority

---

### 2.6 BillingDocument Aggregate

**Aggregate Root**: `BillingDocument`

**Value Objects**:
- `BillingDocument.Type` (enum: INVOICE, RECEIPT, CREDIT_NOTE)
- `BillingDocument.Status`

**Repository**: `BillingDocumentRepository`

**Domain Services**:
- `BillingDocumentManager` - Document generation and management

**Invariants**:
- Billing document must be linked to a confirmed reservation
- Invoice number must be unique and sequential
- Credit notes must reference original invoice
- Document model must be immutable once generated

---

### 2.7 Transaction Aggregate

**Aggregate Root**: `Transaction`

**Value Objects**:
- `Transaction.Status` (enum: PENDING, COMPLETE, FAILED, CANCELLED)
- `PaymentMethod`
- `PaymentProxy`

**Repository**: `TransactionRepository`

**Domain Services**:
- `PaymentManager` - Payment processing orchestration

**Invariants**:
- Transaction must be linked to a reservation
- Amount must match reservation total
- Currency must match event currency
- Completed transactions cannot be modified

---

### 2.8 PromoCode Aggregate

**Aggregate Root**: `PromoCodeDiscount`

**Value Objects**:
- `PromoCodeDiscount.DiscountType` (enum: FIXED_AMOUNT, PERCENTAGE, FIXED_AMOUNT_RESERVATION, NONE)
- `PromoCodeDiscount.CodeType` (enum: DISCOUNT, ACCESS)

**Repository**: `PromoCodeDiscountRepository`

**Domain Services**:
- `PromoCodeRequestManager`
- `SpecialPriceManager`

**Invariants**:
- Promo code must have valid date range
- Discount amount must be valid for type
- Max usage must be >= 0 or unlimited
- Hidden categories require ACCESS code type

---

### 2.9 Poll Aggregate

**Aggregate Root**: `Poll`

**Entities**:
- `Poll` - Root entity
- `PollOption` - Available poll options
- `PollParticipant` - Participant votes

**Value Objects**:
- `PollStatus` (enum: DRAFT, OPEN, CLOSED)

**Repository**: `PollRepository`

**Domain Services**:
- `PollManager` - Poll lifecycle management

**Invariants**:
- Poll must be linked to an event
- Poll options must be unique
- Closed polls cannot accept new votes

---

## 3. Domain Services

### 3.1 Core Domain Services

| Service | Responsibility | Bounded Context |
|---------|---------------|-----------------|
| `EventManager` | Event CRUD, lifecycle, configuration | Event Management |
| `TicketReservationManager` | Reservation creation, modification, cancellation | Reservation & Ticketing |
| `SubscriptionManager` | Subscription descriptor management | Subscription |
| `PaymentManager` | Payment processing coordination | Payment |
| `BillingDocumentManager` | Invoice/receipt generation | Billing |
| `NotificationManager` | Email notifications | Notification |

### 3.2 Supporting Domain Services

| Service | Responsibility |
|---------|---------------|
| `EventStatisticsManager` | Event analytics and reporting |
| `TicketCategoryAvailabilityManager` | Ticket availability calculations |
| `CheckInManager` | Ticket check-in operations |
| `AttendeeManager` | Attendee information management |
| `AdditionalServiceManager` | Additional service booking |
| `GroupManager` | Access group management |
| `WaitingQueueManager` | Waiting list processing |
| `ExtensionManager` | Plugin/extension system |
| `AccessService` | Authorization and access control |
| `SpecialPriceTokenGenerator` | Special price code generation |

---

## 4. Repositories

### 4.1 Aggregate Repositories

| Repository | Aggregate | Type |
|-----------|-----------|------|
| `EventRepository` | Event | Root |
| `TicketReservationRepository` | TicketReservation | Root |
| `TicketRepository` | Ticket | Entity (within Reservation) |
| `SubscriptionRepository` | SubscriptionDescriptor | Root |
| `OrganizationRepository` | Organization | Root |
| `UserRepository` | User | Entity |
| `BillingDocumentRepository` | BillingDocument | Root |
| `TransactionRepository` | Transaction | Root |
| `PromoCodeDiscountRepository` | PromoCodeDiscount | Root |
| `PollRepository` | Poll | Root |

### 4.2 Supporting Repositories

| Repository | Purpose |
|-----------|---------|
| `TicketCategoryRepository` | Ticket category queries |
| `AdditionalServiceRepository` | Additional service queries |
| `TicketFieldRepository` | Custom ticket fields |
| `PurchaseContextFieldRepository` | Generic field configurations |
| `GroupRepository` | Group management |
| `AuditingRepository` | Audit trail |
| `WaitingQueueRepository` | Waiting list management |

---

## 5. Value Objects

### 5.1 Core Value Objects

- **Money & Pricing**:
  - `PriceContainer`
  - `PromoCodeDiscount`
  - `OrderSummary`
  - `SummaryRow`

- **Location & Time**:
  - `ZonedDateTime` (from Java time)
  - `ZoneId`
  - `DatesWithOffset`
  - `DateValidity`

- **Contact Information**:
  - `CustomerName`
  - `BillingDetails`
  - `ContactAndTicketsOwner`

- **Configuration**:
  - `AlfioMetadata`
  - `ConfigurationLevel`
  - `PaymentProxy`

### 5.2 Enumerations (Value Objects)

- `Event.EventFormat`, `Event.Status`
- `Ticket.TicketStatus`
- `TicketReservation.TicketReservationStatus`
- `Transaction.Status`
- `PromoCodeDiscount.DiscountType`, `PromoCodeDiscount.CodeType`
- `SubscriptionDescriptor.Status`, `SubscriptionValidityType`, `SubscriptionUsageType`
- `BillingDocument.Type`
- `AdditionalService.AdditionalServiceType`, `AdditionalService.SupplementPolicy`

---

## 6. Domain Events

### 6.1 Reservation Lifecycle Events

- `ReservationCreated` - New reservation initiated
- `ReservationConfirmed` - Payment completed, reservation confirmed
- `ReservationCancelled` - Reservation cancelled
- `ReservationExpired` - Reservation expired without payment
- `ReservationModified` - Reservation details updated

### 6.2 Ticket Events

- `TicketAssigned` - Ticket assigned to attendee
- `TicketCheckedIn` - Ticket checked in at event
- `TicketReleased` - Ticket released back to pool
- `TicketCancelled` - Ticket cancelled

### 6.3 Payment Events

- `PaymentConfirmed` - Payment successfully processed
- `PaymentFailed` - Payment processing failed
- `RefundIssued` - Refund processed

### 6.4 Audit Events

All domain events are recorded via `AuditingRepository` with:
- `Audit.EventType` (INSERT, UPDATE, DELETE, CANCEL_RESERVATION, CONFIRMATION, etc.)
- `Audit.EntityType` (EVENT, TICKET, RESERVATION, SUBSCRIPTION)

---

## 7. Factories

### 7.1 Event Factory

**Location**: Within `EventManager`

**Responsibilities**:
- Create new events with default configurations
- Validate event creation parameters
- Initialize event metadata

### 7.2 Reservation Factory

**Location**: Within `TicketReservationManager`

**Responsibilities**:
- Create reservations with tickets
- Apply promo codes and discounts
- Calculate pricing and expiration

### 7.3 Subscription Factory

**Location**: Within `SubscriptionManager`

**Responsibilities**:
- Create subscription descriptors
- Validate subscription parameters
- Initialize subscription metadata

---

## 8. Application Services (Orchestration Layer)

### 8.1 API Controllers

| Controller | Purpose | Bounded Context |
|-----------|---------|----------------|
| `EventApiController` | Event CRUD API | Event Management |
| `ReservationApiController` | Reservation operations | Reservation & Ticketing |
| `SubscriptionApiController` | Subscription management | Subscription |
| `AdminReservationController` | Admin reservation operations | Reservation & Ticketing |
| `CheckInApiController` | Check-in operations | Ticketing |
| `TicketApiController` | Ticket operations | Ticketing |
| `UserApiController` | User management | Organization & User |

### 8.2 Coordinators

- `ReservationFinalizer` - Coordinates final reservation confirmation
- `OrganizationDeleter` - Coordinates organization deletion
- `WaitingQueueSubscriptionProcessor` - Processes waiting queue

---

## 9. Integration Points & Anti-Corruption Layers

### 9.1 Payment Gateway Integration

**Anti-Corruption Layer**: `PaymentManager` + specific provider implementations

**External Systems**:
- Stripe
- PayPal
- Bank Transfer
- On-site Payment

**Pattern**: Strategy pattern with `PaymentProvider` interface

### 9.2 Extension System

**Anti-Corruption Layer**: `ExtensionManager`

**Purpose**: Allow custom business logic via scripts/plugins without corrupting core domain

**Technologies**: Rhino JavaScript engine for custom extensions

### 9.3 Email Service

**Anti-Corruption Layer**: `NotificationManager`

**External Service**: SMTP mail servers

**Pattern**: Template-based email generation with domain event subscriptions

---

## 10. Ubiquitous Language

### Core Terms

| Term | Definition | Context |
|------|-----------|---------|
| **Event** | A ticketed occasion (conference, concert, etc.) | Event Management |
| **Ticket Category** | A tier/type of ticket (e.g., VIP, General Admission) | Event Management |
| **Reservation** | A temporary hold on tickets pending payment | Reservation & Ticketing |
| **Purchase Context** | Abstract concept representing Event or Subscription | Cross-cutting |
| **Additional Service** | Optional add-on service (parking, merchandise, etc.) | Event Management |
| **Subscription** | Recurring access pass to multiple events | Subscription |
| **Promo Code** | Discount or access code | Pricing |
| **Special Price** | Unique access code for restricted tickets | Pricing |
| **Billing Document** | Invoice, receipt, or credit note | Billing |
| **Check-in** | Verification of ticket at event entrance | Ticketing |
| **Waiting Queue** | List of users waiting for sold-out tickets | Ticketing |
| **Organization** | Entity managing events (organizer) | Organization |
| **Group** | Collection of users with shared access | Organization |
| **Poll** | Survey associated with an event | Event Management |

### Status Workflows

**Event**: DRAFT → PUBLIC ⇄ DISABLED

**Ticket**: FREE → PENDING → ACQUIRED/TO_BE_PAID → CHECKED_IN

**Reservation**: PENDING → IN_PAYMENT → COMPLETE (or CANCELLED)

**Transaction**: PENDING → COMPLETE/FAILED/CANCELLED

**Subscription**: DRAFT → ACTIVE ⇄ NOT_ACTIVE

---

## 11. Architecture Patterns

### 11.1 CQRS Elements

While not full CQRS, the system shows separation:

**Command Side**:
- Managers (`EventManager`, `TicketReservationManager`, etc.)
- Direct repository writes

**Query Side**:
- Statistics views (`EventStatisticView`)
- Search repositories (`TicketSearchRepository`)
- Read-optimized queries in repositories

### 11.2 Event Sourcing Elements

**Audit Log** acts as limited event store:
- `AuditingRepository` records all state changes
- Can reconstruct entity history
- Not full event sourcing (current state stored in tables)

### 11.3 Layered Architecture

```
┌─────────────────────────────────────────┐
│     Presentation Layer (Controllers)     │
├─────────────────────────────────────────┤
│   Application Layer (Managers/Services) │
├─────────────────────────────────────────┤
│      Domain Layer (Aggregates/VOs)      │
├─────────────────────────────────────────┤
│  Infrastructure Layer (Repositories/DB) │
└─────────────────────────────────────────┘
```

---

## 12. Tactical Patterns Summary

| Pattern | Usage in Alf.io |
|---------|----------------|
| **Aggregate** | Event, TicketReservation, SubscriptionDescriptor, Organization, etc. |
| **Entity** | Event, Ticket, TicketReservation, User, Organization |
| **Value Object** | EventFormat, TicketStatus, BillingDetails, OrderSummary, Money values |
| **Repository** | All `*Repository` interfaces using NPJT framework |
| **Domain Service** | All `*Manager` classes (EventManager, TicketReservationManager, etc.) |
| **Factory** | Embedded in Managers (e.g., createTicketReservation) |
| **Domain Event** | Audit events, extension system hooks |
| **Specification** | Category availability checks, validation rules |

---

## 13. Strategic Design

### 13.1 Core Domain

The **core domain** (competitive advantage) is:
- Event Management with flexible ticketing
- Smart reservation system with pricing rules
- Subscription-based access model

### 13.2 Supporting Subdomains

- Billing & Invoicing
- Payment Processing
- Notification System
- Check-in Management

### 13.3 Generic Subdomains

- User & Organization Management
- Access Control & Authorization
- File Management
- Audit Logging

---

## 14. Context Map

```
┌──────────────────┐
│   Organization   │
│     Context      │
└────────┬─────────┘
         │ Open Host Service
         ▼
┌──────────────────┐      ┌──────────────────┐
│  Event Management├─────►│   Subscription   │
│     Context      │ PL   │     Context      │
└────────┬─────────┘      └──────────────────┘
         │ Conformist
         ▼
┌──────────────────┐      ┌──────────────────┐
│  Reservation &   │◄─────┤     Payment      │
│    Ticketing     │ ACL  │     Context      │
└────────┬─────────┘      └──────────────────┘
         │ Published Language
         ▼
┌──────────────────┐      ┌──────────────────┐
│     Billing      │      │   Notification   │
│     Context      │      │     Context      │
└──────────────────┘      └──────────────────┘

Legend:
PL = Published Language
ACL = Anti-Corruption Layer
```

### Relationships:

1. **Organization → Event Management**: Open Host Service - Organization provides user/org data
2. **Event Management → Subscription**: Partnership - Both create purchase contexts
3. **Event Management → Reservation**: Upstream/Downstream - Events feed reservation system
4. **Reservation → Payment**: Anti-Corruption Layer - PaymentManager shields domain from gateways
5. **Reservation → Billing**: Downstream - Confirmed reservations trigger billing
6. **All → Notification**: Shared Kernel - All contexts use notification service

---

## 15. Implementation Notes

### 15.1 Technology Stack

- **Persistence**: PostgreSQL with row-level security
- **ORM**: NPJT (Nano Persistence JDBC Toolkit) - minimal, type-safe SQL mapper
- **Framework**: Spring Boot 3.3.5
- **Java**: Version 17
- **Build**: Maven (converted from Gradle)

### 15.2 Database Design

- **Schema**: Relational with normalized tables
- **Audit**: Comprehensive auditing via `auditing` table
- **Migrations**: Flyway for version control
- **Row Security**: PostgreSQL RLS for multi-tenancy

### 15.3 Testing Strategy

- **Unit Tests**: Domain logic validation
- **Integration Tests**: TestContainers with real PostgreSQL
- **E2E Tests**: Selenium for critical user flows

---

## 16. Future Enhancements & Refactoring Opportunities

### 16.1 Potential Improvements

1. **Full CQRS**: Separate read/write models completely
2. **Event Sourcing**: Move from audit log to full event store
3. **Saga Pattern**: For complex multi-step transactions
4. **Domain Events Bus**: Explicit event publishing/subscription
5. **Bounded Context Isolation**: Separate databases per context

### 16.2 Aggregate Refinement

- Consider splitting large aggregates (e.g., Event with many categories)
- Evaluate invariants to ensure proper aggregate boundaries
- Add more explicit domain events for state transitions

---

## 17. Conclusion

The Alf.io system demonstrates solid DDD principles:

✅ **Clear bounded contexts** around Event, Reservation, Subscription, Billing
✅ **Rich domain model** with well-defined aggregates and value objects
✅ **Ubiquitous language** reflected in code (Event, Ticket, Reservation, etc.)
✅ **Repository pattern** for aggregate persistence
✅ **Domain services** for complex business logic
✅ **Audit/Event log** for traceability

**Strengths**:
- Well-defined aggregate boundaries
- Strong invariant enforcement
- Clear separation of concerns
- Comprehensive audit trail

**Areas for Improvement**:
- More explicit domain events
- Consider CQRS for read-heavy operations
- Potential for saga pattern in complex workflows
- Further modularization of bounded contexts

---

**Document Version**: 1.0
**Last Updated**: February 12, 2026
**Maintained By**: Development Team

