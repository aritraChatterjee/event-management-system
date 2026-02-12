# DDD Quick Reference Guide - Alf.io

## Aggregate Roots Quick Reference

### Core Aggregates

| Aggregate | Package | Repository | Key Manager |
|-----------|---------|-----------|-------------|
| `Event` | `alfio.model` | `EventRepository` | `EventManager` |
| `TicketReservation` | `alfio.model` | `TicketReservationRepository` | `TicketReservationManager` |
| `SubscriptionDescriptor` | `alfio.model.subscription` | `SubscriptionRepository` | `SubscriptionManager` |
| `Organization` | `alfio.model.user` | `OrganizationRepository` | `AccessService` |
| `BillingDocument` | `alfio.model` | `BillingDocumentRepository` | `BillingDocumentManager` |
| `Transaction` | `alfio.model.transaction` | `TransactionRepository` | `PaymentManager` |
| `PromoCodeDiscount` | `alfio.model` | `PromoCodeDiscountRepository` | `PromoCodeRequestManager` |
| `Poll` | `alfio.model.poll` | `PollRepository` | `PollManager` |

---

## Key Domain Services Cheat Sheet

### Event Management
```java
EventManager.createEvent(...)
EventManager.updateEventHeader(...)
EventManager.toggleActiveFlag(...)
EventStatisticsManager.getTicketSoldStatistics(...)
```

### Reservation & Ticketing
```java
TicketReservationManager.createTicketReservation(...)
TicketReservationManager.confirmOfflinePayment(...)
TicketReservationManager.cancelPendingReservation(...)
CheckInManager.checkIn(...)
AttendeeManager.updateTicketOwner(...)
```

### Subscription
```java
SubscriptionManager.createSubscriptionDescriptor(...)
SubscriptionReservationManager.createSubscriptionReservation(...)
```

### Billing
```java
BillingDocumentManager.ensureBillingDocumentIsPresent(...)
BillingDocumentManager.createCreditNote(...)
```

### Payment
```java
PaymentManager.processPayment(...)
PaymentManager.refund(...)
```

---

## Common Value Objects

### Pricing
```java
PriceContainer - Interface for price calculations
OrderSummary - Complete order breakdown
SummaryRow - Individual line item
PromoCodeDiscount - Discount value object
```

### Status Enums
```java
Event.Status: DRAFT, PUBLIC, DISABLED
Ticket.TicketStatus: FREE, PENDING, ACQUIRED, CHECKED_IN, etc.
TicketReservation.Status: PENDING, IN_PAYMENT, COMPLETE, CANCELLED, etc.
Transaction.Status: PENDING, COMPLETE, FAILED, CANCELLED
```

### Contact & Billing
```java
CustomerName - Name value object
BillingDetails - Billing address and tax info
ContactAndTicketsOwner - Owner contact information
```

---

## Repository Query Patterns

### Finding by ID
```java
eventRepository.findById(int id)
ticketReservationRepository.findReservationById(String id)
subscriptionRepository.findOne(UUID id)
```

### Finding with Relations
```java
ticketRepository.findTicketsInReservation(String reservationId)
ticketCategoryRepository.findCategoriesInReservation(String reservationId)
```

### Locking for Update
```java
eventRepository.findOptionalByShortNameForUpdate(String shortName)
ticketRepository.findTicketsToBeAssignedByReservationId(String reservationId)
```

### Statistical Queries
```java
eventRepository.findStatisticsFor(int eventId)
ticketRepository.countTicketsInReservation(String reservationId)
```

---

## Invariant Enforcement Examples

### Event Aggregate
```java
// In EventManager
- Validate start < end dates
- Ensure currency is valid
- Check organization exists
- Validate payment proxies
- Available seats >= 0
```

### TicketReservation Aggregate
```java
// In TicketReservationManager
- Expiration date must be in future
- All tickets belong to same event
- Total price matches calculation
- Status transitions are valid
- Cannot modify completed reservation
```

### Ticket Entity
```java
// In Ticket domain logic
- Cannot check-in FREE ticket
- CHECKED_IN tickets cannot be released
- Category must exist for event
- Locked assignment cannot change
```

---

## Domain Event Examples

### Audit Events (via AuditingRepository)
```java
// Event Types
RESERVATION_CREATE, RESERVATION_COMPLETE, RESERVATION_CANCEL
TICKET_ASSIGN, TICKET_CHECK_IN, TICKET_RELEASE
PAYMENT_CONFIRMED, PAYMENT_FAILED
UPDATE_EVENT, UPDATE_TICKET_CATEGORY

// Entity Types
EVENT, TICKET, RESERVATION, SUBSCRIPTION

// Usage
auditingRepository.insert(reservationId, userId, eventId, 
    Audit.EventType.RESERVATION_COMPLETE, 
    new Date(), 
    Audit.EntityType.RESERVATION, 
    reservationId);
```

---

## Factory Methods

### Event Creation
```java
// In EventManager
EventModification modification = ...;
Event event = EventManager.createEvent(modification);
```

### Reservation Creation
```java
// In TicketReservationManager
String reservationId = ticketReservationManager.createTicketReservation(
    event,
    ticketRequests,
    additionalServices,
    expirationDate,
    promoCode,
    locale,
    forWaitingQueue,
    principal
);
```

---

## Access Control Patterns

### Organization Access
```java
// In AccessService
accessService.checkOrganizationOwnership(principal, organizationId);
accessService.ensureEventOwnership(principal, eventId);
```

### Event Access
```java
accessService.checkEventOwnership(principal, eventId);
accessService.checkCategoryOwnership(principal, eventId, categoryId);
```

### User Authorities
```java
ROLE_ADMIN - System administrator
ROLE_OWNER - Organization owner
ROLE_OPERATOR - Event operator
ROLE_SPONSOR - Limited sponsor access
```

---

## Integration Points

### Payment Gateway
```java
// Anti-Corruption Layer: PaymentManager
PaymentProvider provider = paymentManager.lookupProviderByMethod(...);
PaymentResult result = provider.doPayment(specification);
```

### Email Notifications
```java
// Via NotificationManager
notificationManager.sendSimpleEmail(event, reservation, recipient, 
    subject, textBuilder);
```

### Extension System
```java
// Via ExtensionManager
extensionManager.handleReservationConfirmation(event, reservationId);
extensionManager.handleTicketAssignment(ticket);
```

---

## Transaction Boundaries

### Read-Only Transactions
```java
@Transactional(readOnly = true)
public List<Event> findAll() { ... }
```

### Write Transactions
```java
@Transactional
public void confirmOfflinePayment(Event event, String reservationId) { 
    // Multiple updates within single transaction
    ticketReservationRepository.updateStatus(...);
    ticketRepository.updateTicketStatus(...);
    auditingRepository.insert(...);
}
```

### Optimistic Locking
```java
// Using version column in Event
event.getVersion(); // Checked on update
```

---

## Common Validation Patterns

### Category Availability
```java
TicketCategoryAvailabilityManager.countAvailableTickets(event, category);
SaleableTicketCategory.getSaleable(); // Boolean check
```

### Reservation Validation
```java
ReservationUtil.validateCategory(errors, manager, event, ...);
```

### Payment Validation
```java
PaymentManager.isValidPaymentMethod(method, event);
```

---

## Testing Patterns

### Integration Test Setup
```java
@AlfioIntegrationTest
@ContextConfiguration(classes = TestConfig.class)
class EventRepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired EventRepository eventRepository;
    @Autowired TestContainers testContainers; // PostgreSQL
}
```

### Domain Logic Unit Tests
```java
// Test aggregate invariants
@Test
void testEventEndDateMustBeAfterStart() { ... }
```

---

## Performance Considerations

### Lazy Loading
```java
// Load related entities only when needed
event.getTicketCategories(); // Separate query if needed
```

### Batch Operations
```java
ticketRepository.resetCategoryIdForUnboundedCategories(reservationIds);
// Single SQL for multiple tickets
```

### Caching
```java
@Cacheable("events")
public Event findById(int id) { ... }
```

---

## Migration Notes (Gradle → Maven)

### Building
```bash
# Old: ./gradlew clean build
mvn clean package

# Old: ./gradlew bootRun -Pprofile=dev
mvn spring-boot:run -Pdev
```

### Testing
```bash
# Old: ./gradlew test
mvn test

# Old: ./gradlew integrationTest
mvn verify
```

### Frontend Build
```bash
# Automatically handled by frontend-maven-plugin
# Runs: npm ci && npm run build for both frontend/ and frontend/admin/
```

---

## Useful SQL Patterns (NPJT)

### Simple Query
```java
@Query("select * from event where id = :id")
Event findById(@Bind("id") int id);
```

### With JSON
```java
@Query("update event set metadata = :metadata::jsonb where id = :id")
int updateMetadata(@Bind("metadata") @JSONData AlfioMetadata metadata, @Bind("id") int id);
```

### Auto-Generated Key
```java
@Query("insert into event(...) values(...)")
@AutoGeneratedKey("id")
AffectedRowCountAndKey<Integer> insert(...);
```

### Batch Update
```java
@Query("update ticket set status = :status where id in (:ids)")
int updateStatuses(@Bind("ids") Collection<Integer> ids, @Bind("status") TicketStatus status);
```

---

## Common Pitfalls & Solutions

### ❌ Modifying Completed Reservation
**Problem**: Trying to change tickets in COMPLETE reservation
**Solution**: Check reservation status before modifications

### ❌ Breaking Aggregate Boundaries
**Problem**: Directly modifying Ticket without going through Reservation
**Solution**: Always use aggregate root for modifications

### ❌ Missing Transaction
**Problem**: Multiple updates without transaction, partial failure
**Solution**: Wrap in `@Transactional` method

### ❌ N+1 Query Problem
**Problem**: Loading tickets one-by-one in loop
**Solution**: Use batch queries or joins

---

**Quick Reference Version**: 1.0
**Last Updated**: February 12, 2026

