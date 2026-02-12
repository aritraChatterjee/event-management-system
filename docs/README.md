# Alf.io Domain-Driven Design Documentation

This directory contains comprehensive Domain-Driven Design (DDD) documentation for the Alf.io event management system.

## 📚 Documentation Overview

### Domain-Driven Design (DDD)

#### [DDD_MAPPING.md](./DDD_MAPPING.md)
**Complete DDD Analysis** - The main reference document containing:
- Bounded contexts identification
- Aggregate definitions with entities and value objects
- Repository patterns
- Domain services
- Ubiquitous language
- Strategic design patterns
- Context mapping
- Implementation notes

**Best for**: Understanding the complete domain model and strategic design decisions.

### [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md)
**Developer Cheat Sheet** - Quick lookup guide containing:
- Aggregate root quick reference table
- Common domain service methods
- Value object listings
- Repository query patterns
- Invariant enforcement examples
- Domain event examples
- Factory method patterns
- Testing patterns
- Migration notes (Gradle → Maven)

**Best for**: Day-to-day development work and quick lookups.

### [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md)
**Visual Reference** - Mermaid diagrams including:
- Bounded context diagram
- Aggregate class diagrams (Event, Reservation, Subscription, Organization)
- Sequence diagrams (Transaction flow)
- State machines (Reservation, Ticket, Event lifecycle)
- Flowcharts (Payment, Check-in, Promo code)
- Extension system architecture
- Database schema overview
- Layered architecture

**Best for**: Visual learners and architectural discussions.

### Database Documentation

#### [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)
**Complete Schema Reference** - Comprehensive database documentation:
- All table structures with column details
- Constraints and indexes
- Foreign key relationships
- Row-level security policies
- Views and materialized views
- Migration strategy
- Best practices

**Best for**: Understanding database structure, writing queries, and database administration.

#### [DATABASE_ER_DIAGRAMS.md](./DATABASE_ER_DIAGRAMS.md)
**Entity-Relationship Diagrams** - Visual database schema using Mermaid:
- Complete schema overview
- Domain-specific ER diagrams
- Table relationships
- Enum type definitions
- Cardinality mappings

**Best for**: Visual understanding of data relationships and database design.

#### [DATABASE_QUERIES.md](./DATABASE_QUERIES.md)
**Query Reference Guide** - Common queries and patterns:
- Event, ticket, and reservation queries
- Reporting and statistics queries
- User and access control queries
- Billing and payment queries
- Performance and maintenance queries
- Best practices and optimization tips

**Best for**: Daily database work, writing reports, and query optimization.

### API Documentation

#### [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
**Complete API Reference** - Full REST API documentation:
- Public API (v2) endpoints
- Admin API endpoints  
- Authentication methods
- Request/response examples
- Error handling
- Rate limiting
- Webhooks
- Payment API

**Best for**: API integration, building clients, and third-party integrations.

#### [API_QUICK_REFERENCE.md](./API_QUICK_REFERENCE.md)
**API Quick Start Guide** - Practical API usage:
- Common curl examples
- Quick start tutorials
- Request snippets (JavaScript, Python)
- Common workflows
- Best practices
- Testing tips

**Best for**: Quick API lookups, copy-paste examples, and rapid prototyping.

---

## 🎯 Quick Start Guide

### For New Developers

1. **Start with**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) - Get visual overview
2. **Read**: [DDD_MAPPING.md](./DDD_MAPPING.md) sections 1-2 - Understand bounded contexts
3. **Study**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Learn database structure
4. **Reference**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) - While coding

### For API Developers

1. **Start with**: [API_QUICK_REFERENCE.md](./API_QUICK_REFERENCE.md) - Quick examples
2. **Read**: [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Complete API reference
3. **Map to**: [DDD_MAPPING.md](./DDD_MAPPING.md) - Understand domain model
4. **Test**: Use curl examples and sandbox environment

### For Database Developers

1. **Start with**: [DATABASE_ER_DIAGRAMS.md](./DATABASE_ER_DIAGRAMS.md) - Visual schema
2. **Read**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Complete table reference
3. **Use**: [DATABASE_QUERIES.md](./DATABASE_QUERIES.md) - Query examples
4. **Map to**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 4 - Repositories

### For Architects

1. **Review**: [DDD_MAPPING.md](./DDD_MAPPING.md) sections 1, 11-14 - Strategic design
2. **Study**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) - Context maps and boundaries
3. **Examine**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Data architecture
4. **Evaluate**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 16 - Future enhancements

### For Product Owners

1. **Understand**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 10 - Ubiquitous language
2. **Review**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) state machines - Business workflows
3. **Learn**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 13 - Core vs supporting domains
4. **Explore**: [DATABASE_ER_DIAGRAMS.md](./DATABASE_ER_DIAGRAMS.md) - Data relationships

---

## 🔑 Key Concepts at a Glance

### Core Bounded Contexts

| Context | Focus | Key Aggregates |
|---------|-------|----------------|
| **Event Management** | Creating and managing events | Event, TicketCategory, AdditionalService |
| **Reservation & Ticketing** | Booking and ticket lifecycle | TicketReservation, Ticket |
| **Subscription** | Recurring access passes | SubscriptionDescriptor, Subscription |

### Primary Aggregates

```
Event
├─ TicketCategory (1..*)
├─ AdditionalService (0..*)
└─ EventDescription (0..*)

TicketReservation
├─ Ticket (1..*)
├─ AdditionalServiceItem (0..*)
└─ BillingDetails (1)

SubscriptionDescriptor
├─ Subscription (0..*)
└─ EventSubscriptionLink (0..*)
```

### Ubiquitous Language Highlights

- **Event**: A ticketed occasion (conference, concert, meetup)
- **Ticket Category**: Tier/type of ticket (VIP, General Admission, Early Bird)
- **Reservation**: Temporary hold on tickets pending payment
- **Purchase Context**: Abstract concept representing Event or Subscription
- **Additional Service**: Optional add-on (parking, merchandise, workshop)
- **Promo Code**: Discount or access code for tickets
- **Check-in**: Verification of ticket at event entrance

---

## 📖 Navigation by Topic

### API Integration
- **Quick start**: [API_QUICK_REFERENCE.md](./API_QUICK_REFERENCE.md) - Curl examples
- **Complete reference**: [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - All endpoints
- **Domain model**: [DDD_MAPPING.md](./DDD_MAPPING.md) - Understand entities
- **Data structure**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Response formats

### Understanding Aggregates
- **Full details**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 2
- **Class diagrams**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) sections 2-5
- **Quick lookup**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Aggregate Roots Quick Reference"
- **Database tables**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Core Entities
- **API endpoints**: [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - CRUD operations

### Working with Repositories
- **Patterns**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 4
- **Query examples**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Repository Query Patterns"
- **NPJT syntax**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Useful SQL Patterns"
- **SQL queries**: [DATABASE_QUERIES.md](./DATABASE_QUERIES.md) - All categories

### Database Work
- **Schema reference**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Complete documentation
- **Visual diagrams**: [DATABASE_ER_DIAGRAMS.md](./DATABASE_ER_DIAGRAMS.md) - All ER diagrams
- **Query examples**: [DATABASE_QUERIES.md](./DATABASE_QUERIES.md) - By use case
- **Migrations**: `src/main/resources/alfio/db/PGSQL/` - Flyway scripts

### Domain Services
- **Overview**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 3
- **Method signatures**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Key Domain Services Cheat Sheet"
- **Flow diagrams**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) sections 6, 10-12
- **Data access**: [DATABASE_QUERIES.md](./DATABASE_QUERIES.md) - Repository queries

### Business Workflows
- **State machines**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) sections 7-9
- **Process flows**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) sections 10-12
- **Invariants**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Invariant Enforcement Examples"
- **Database constraints**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Constraints section

### Reporting & Analytics
- **Query examples**: [DATABASE_QUERIES.md](./DATABASE_QUERIES.md) "Reporting & Statistics"
- **Views**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) "Views" section
- **Statistics**: [DATABASE_ER_DIAGRAMS.md](./DATABASE_ER_DIAGRAMS.md) - Statistics views

### Integration & Anti-Corruption Layers
- **Design**: [DDD_MAPPING.md](./DDD_MAPPING.md) section 9
- **Extension system**: [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md) section 13
- **Patterns**: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Integration Points"
- **Database hooks**: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Extension System

---

## 🏗️ Architecture Patterns Used

### Tactical Patterns
- ✅ **Aggregates**: Event, TicketReservation, SubscriptionDescriptor, Organization
- ✅ **Entities**: Ticket, TicketCategory, User, Group
- ✅ **Value Objects**: EventFormat, TicketStatus, BillingDetails, OrderSummary
- ✅ **Repositories**: All `*Repository` interfaces
- ✅ **Domain Services**: All `*Manager` classes
- ✅ **Factories**: Embedded in Managers
- ✅ **Domain Events**: Audit events, extension hooks

### Strategic Patterns
- ✅ **Bounded Contexts**: Event, Reservation, Subscription, Billing, etc.
- ✅ **Context Mapping**: Anti-corruption layers, Published language
- ✅ **Core Domain**: Event management with ticketing
- ✅ **Supporting Subdomain**: Billing, Payment, Notifications
- ✅ **Generic Subdomain**: User management, Audit logging

### Architectural Styles
- **Layered Architecture**: Presentation → Application → Domain → Infrastructure
- **CQRS Elements**: Separate read/write repositories, statistics views
- **Event Sourcing Elements**: Comprehensive audit log
- **Repository Pattern**: NPJT-based data access

---

## 🔧 Development Workflow

### Making Changes to Aggregates

1. **Identify the aggregate**: Use [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md)
2. **Check invariants**: Review [DDD_MAPPING.md](./DDD_MAPPING.md) aggregate section
3. **Update aggregate root**: Ensure consistency
4. **Update repository**: Add/modify queries
5. **Update domain service**: Orchestrate changes
6. **Emit domain events**: Via AuditingRepository
7. **Test invariants**: Unit + integration tests

### Adding New Features

1. **Identify bounded context**: Review [DDD_MAPPING.md](./DDD_MAPPING.md) section 1
2. **Check ubiquitous language**: Section 10
3. **Design aggregate**: Follow existing patterns
4. **Create repository**: Follow NPJT conventions
5. **Implement domain service**: Coordinate operations
6. **Add integration points**: Use anti-corruption layers if needed
7. **Update documentation**: Add to this guide

---

## 📊 Metrics & Quality

### Aggregate Boundaries
- **Event**: Well-defined, clear ownership
- **TicketReservation**: Strong consistency, proper transactional boundaries
- **Subscription**: Independent lifecycle, good separation
- **Organization**: Clean separation of user management

### Domain Model Maturity
- ✅ Rich domain model (not anemic)
- ✅ Invariants enforced at aggregate boundaries
- ✅ Business logic in domain layer
- ✅ Clear separation of concerns
- ⚠️ Could improve with more explicit domain events
- ⚠️ Some aggregates could be further refined

---

## 🧪 Testing Strategy

### Unit Tests
- Test aggregate invariants
- Test value object equality
- Test domain logic in isolation
- See: [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) "Testing Patterns"

### Integration Tests
- Test repository operations
- Test transaction boundaries
- Use TestContainers with real PostgreSQL
- See: `src/test/java/alfio/repository/*Test.java`

### Domain Behavior Tests
- Test business workflows
- Test state transitions
- Test invariant violations
- See: `src/test/java/alfio/manager/*Test.java`

---

## 📝 Contributing to Documentation

### When to Update

- ✏️ **New aggregate added**: Update all 3 documents
- ✏️ **Bounded context changed**: Update DDD_MAPPING.md section 1 + DDD_DIAGRAMS.md section 1
- ✏️ **New domain service**: Update DDD_QUICK_REFERENCE.md + DDD_MAPPING.md section 3
- ✏️ **Business workflow changed**: Update DDD_DIAGRAMS.md state machines/flowcharts
- ✏️ **Invariant added**: Update DDD_MAPPING.md aggregate section + DDD_QUICK_REFERENCE.md

### Update Checklist

- [ ] Update version number in document footer
- [ ] Update "Last Updated" date
- [ ] Cross-reference between documents
- [ ] Test Mermaid diagrams render correctly
- [ ] Review for consistency with codebase

---

## 🔗 Related Documentation

### Project Documentation
- `../README.md` - Main project README
- `../CHANGELOG.md` - Version history
- `../CODE_OF_CONDUCT.md` - Community guidelines

### Technical Documentation
- `../src/main/resources/` - Database migrations (Flyway)
- `../pom.xml` - Maven project configuration
- `../docker-compose.yml` - Development environment setup

### API Documentation
- OpenAPI spec generated at runtime: `/admin/api/openapi.json`
- Admin API: `/admin/api/`
- Public API: `/api/v2/`

---

## 🎓 Learning Resources

### DDD Books & Resources
- **Domain-Driven Design** by Eric Evans (Blue Book)
- **Implementing Domain-Driven Design** by Vaughn Vernon (Red Book)
- **Domain-Driven Design Distilled** by Vaughn Vernon

### Spring & Java Resources
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- NPJT Documentation: https://github.com/digitalfondue/npjt

### Architecture Patterns
- **Patterns of Enterprise Application Architecture** by Martin Fowler
- **Clean Architecture** by Robert C. Martin

---

## 🆘 Getting Help

### Quick Questions
- Check [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md) first
- Search in [DDD_MAPPING.md](./DDD_MAPPING.md) for detailed explanations

### Design Questions
- Review relevant aggregate in [DDD_MAPPING.md](./DDD_MAPPING.md) section 2
- Check state machines in [DDD_DIAGRAMS.md](./DDD_DIAGRAMS.md)

### Implementation Questions
- See code examples in [DDD_QUICK_REFERENCE.md](./DDD_QUICK_REFERENCE.md)
- Review actual implementation in `src/main/java/alfio/`

---

## 📅 Maintenance Schedule

- **Quarterly Review**: Ensure documentation matches current codebase
- **After Major Releases**: Update with new features/changes
- **On Architecture Changes**: Immediate update required

**Last Documentation Review**: February 12, 2026  
**Next Scheduled Review**: May 12, 2026

---

**Documentation Maintained By**: Development Team  
**Version**: 1.0  
**Status**: ✅ Current

