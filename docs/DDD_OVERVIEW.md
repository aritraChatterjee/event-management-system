# Alf.io Domain-Driven Design - Visual Overview

## 🎯 System at a Glance

```
┌─────────────────────────────────────────────────────────────────┐
│                    ALFIO EVENT MANAGEMENT SYSTEM                 │
│                   Open Source Ticketing Platform                 │
└─────────────────────────────────────────────────────────────────┘

Core Domain: Event Ticketing & Reservations
Supporting: Billing, Payment Processing, Notifications
Generic: User Management, Auditing
```

---

## 🏗️ Bounded Contexts Map

```
┌─────────────────────────────────────────────────────────────────┐
│                          CORE DOMAIN                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐      ┌──────────────────┐                │
│  │ Event Management │──────│  Subscription    │                │
│  │                  │      │                  │                │
│  │ • Event          │      │ • Descriptor     │                │
│  │ • Categories     │      │ • Instances      │                │
│  │ • Services       │      │ • Event Links    │                │
│  └────────┬─────────┘      └──────────────────┘                │
│           │                                                      │
│           ▼                                                      │
│  ┌──────────────────┐                                           │
│  │ Reservation &    │                                           │
│  │   Ticketing      │                                           │
│  │                  │                                           │
│  │ • Reservations   │                                           │
│  │ • Tickets        │                                           │
│  │ • Add-on Items   │                                           │
│  └────────┬─────────┘                                           │
│           │                                                      │
└───────────┼──────────────────────────────────────────────────────┘
            │
            ├───► Payment Context (Anti-Corruption Layer)
            ├───► Billing Context (Invoice Generation)
            └───► Notification Context (Email Service)

┌─────────────────────────────────────────────────────────────────┐
│                        GENERIC SUBDOMAINS                        │
├─────────────────────────────────────────────────────────────────┤
│  Organization & User Management  │  Audit & Logging             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Core Aggregates

### 1️⃣ Event Aggregate
```
EVENT [Root]
├── TicketCategory (1..*)
│   ├── Name, Price, Capacity
│   └── Sale Period
├── AdditionalService (0..*)
│   ├── Type, Price, Quantity
│   └── Availability
└── EventDescription (0..*)
    └── Localized Content

Invariants:
✓ End date > Start date
✓ Available seats >= 0
✓ Valid payment methods configured
```

### 2️⃣ TicketReservation Aggregate
```
TICKET_RESERVATION [Root]
├── Ticket (1..*)
│   ├── Category, Status
│   ├── Attendee Info
│   └── Check-in Status
├── AdditionalServiceItem (0..*)
│   └── Service, Quantity, Price
└── BillingDetails (1)
    └── Address, Tax Info

Invariants:
✓ All tickets from same event
✓ Valid expiration date
✓ Price = Sum(tickets + services)
✓ Status transitions follow workflow
```

### 3️⃣ SubscriptionDescriptor Aggregate
```
SUBSCRIPTION_DESCRIPTOR [Root]
├── Subscription (0..*)
│   ├── Owner, Valid Period
│   └── Usage Tracking
└── EventSubscriptionLink (0..*)
    └── Event, Price Override

Invariants:
✓ Valid date range
✓ Max available >= 0
✓ Usage type consistency
```

---

## 🔄 Key Workflows

### Ticket Purchase Flow
```
1. SELECT TICKETS
   ↓
2. CREATE RESERVATION (PENDING)
   ↓
3. ENTER DETAILS
   ↓
4. APPLY PROMO CODE (optional)
   ↓
5. CHOOSE PAYMENT METHOD
   ↓
6. PROCESS PAYMENT (IN_PAYMENT)
   ↓
7. CONFIRM RESERVATION (COMPLETE)
   ↓
8. GENERATE INVOICE
   ↓
9. SEND CONFIRMATION EMAIL
```

### State Transitions

**Reservation States:**
```
PENDING → IN_PAYMENT → COMPLETE
   ↓           ↓
CANCELLED  CANCELLED
```

**Ticket States:**
```
FREE → PENDING → ACQUIRED → CHECKED_IN
         ↓
     RELEASED → FREE
```

---

## 🎨 Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│  REST Controllers | Web Controllers | Admin/Public UI       │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                          │
│  EventManager | ReservationManager | SubscriptionManager    │
│  PaymentManager | BillingDocumentManager                    │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                             │
│  Aggregates | Entities | Value Objects | Domain Services    │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                         │
│  Repositories (NPJT) | PostgreSQL | Email | Payment Gateway │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗂️ Ubiquitous Language

| Term | Meaning | Context |
|------|---------|---------|
| **Event** | Ticketed occasion | Event Management |
| **Ticket Category** | Tier/type of ticket | Event Management |
| **Reservation** | Temporary ticket hold | Reservation |
| **Purchase Context** | Event or Subscription | Cross-cutting |
| **Additional Service** | Optional add-on | Event Management |
| **Promo Code** | Discount/access code | Pricing |
| **Check-in** | Ticket verification | Ticketing |
| **Billing Document** | Invoice/receipt | Billing |
| **Subscription** | Recurring access pass | Subscription |

---

## 📊 Key Metrics

```
Bounded Contexts:        7
Core Aggregates:         9
Domain Services:        15+
Repositories:           20+
Value Objects:          30+
State Machines:          3
```

---

## 🔧 Technology Stack

```
Backend:     Java 17 + Spring Boot 3.3.5
Database:    PostgreSQL 10+
ORM:         NPJT (Nano Persistence JDBC Toolkit)
Build:       Maven 3.6+
Frontend:    Angular + TypeScript
Testing:     JUnit 5 + TestContainers
```

---

## ✅ DDD Patterns Applied

### Tactical Patterns
- ✅ **Aggregates** - Event, Reservation, Subscription
- ✅ **Entities** - Ticket, Category, User
- ✅ **Value Objects** - Money, Status, Dates
- ✅ **Repositories** - Data access abstraction
- ✅ **Domain Services** - Complex business logic
- ✅ **Factories** - Aggregate creation
- ✅ **Domain Events** - Audit trail

### Strategic Patterns
- ✅ **Bounded Contexts** - Clear boundaries
- ✅ **Context Mapping** - Defined relationships
- ✅ **Anti-Corruption Layer** - Payment integration
- ✅ **Shared Kernel** - Notification service
- ✅ **Published Language** - API contracts

---

## 🚀 Quick Start

### Build & Run
```bash
# Build project
mvn clean package

# Run locally
mvn spring-boot:run -Pdev

# Run tests
mvn test

# Generate coverage
mvn verify
```

### Database Setup
```bash
# Start PostgreSQL
docker run -d --name alfio-db \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=alfio \
  postgres

# Application auto-creates schema
```

### Access
- **Admin UI**: http://localhost:8080/admin
- **Public UI**: http://localhost:8080
- **Default Login**: admin / (see console for password)

---

## 📚 Documentation

Full documentation available in `docs/` directory:

- **[DDD Mapping](./docs/DDD_MAPPING.md)** - Complete domain analysis
- **[Quick Reference](./docs/DDD_QUICK_REFERENCE.md)** - Developer cheat sheet
- **[Diagrams](./docs/DDD_DIAGRAMS.md)** - Visual reference
- **[Guide](./docs/README.md)** - Navigation help

---

## 🎯 Benefits of DDD Approach

### For Development
✅ Clear responsibilities  
✅ Well-defined boundaries  
✅ Rich domain model  
✅ Easy to test  
✅ Maintainable codebase  

### For Business
✅ Common language  
✅ Business logic visible  
✅ Flexible to change  
✅ Domain expertise preserved  
✅ Reduced miscommunication  

### For Architecture
✅ Modular design  
✅ Independent contexts  
✅ Clear integration points  
✅ Scalable structure  
✅ Evolution-friendly  

---

## 🔮 Future Enhancements

1. **Full CQRS** - Separate read/write models
2. **Event Sourcing** - Complete event store
3. **Sagas** - Long-running transactions
4. **Microservices** - Split bounded contexts
5. **Domain Events Bus** - Explicit pub/sub

---

**Version**: 1.0  
**Last Updated**: February 12, 2026  
**License**: GPL v3  
**Website**: https://alf.io

