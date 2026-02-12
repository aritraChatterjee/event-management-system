# Database Schema Documentation - Alf.io Event Management System

## Overview

The Alf.io database schema is designed using PostgreSQL with a focus on:
- **Multi-tenancy** via organizations
- **Row-level security** for data isolation
- **ACID compliance** for transactional integrity
- **Flyway migrations** for version control
- **Materialized views** for performance optimization

**Database**: PostgreSQL 10+  
**Migration Tool**: Flyway  
**ORM**: NPJT (Nano Persistence JDBC Toolkit)  
**Schema Version**: 2.0.0.60+

---

## Table of Contents

1. [Core Entities](#core-entities)
2. [Event Management](#event-management)
3. [Ticket & Reservation](#ticket--reservation)
4. [Subscription](#subscription)
5. [Billing & Payment](#billing--payment)
6. [User & Organization](#user--organization)
7. [Audit & Logging](#audit--logging)
8. [Configuration](#configuration)
9. [Views](#views)
10. [Indexes](#indexes)
11. [Constraints](#constraints)
12. [Row-Level Security](#row-level-security)

---

## Core Entities

### Entity-Relationship Diagram (High-Level)

```
┌──────────────┐
│ Organization │
└──────┬───────┘
       │ 1:N
       ├──────────────────┬──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
  ┌────────┐      ┌──────────────┐   ┌──────────────────┐
  │ Event  │      │ Subscription │   │ User (ba_user)   │
  └───┬────┘      │  Descriptor  │   └──────────────────┘
      │           └──────┬───────┘
      │ 1:N              │ 1:N
      ▼                  ▼
┌────────────────┐ ┌──────────────┐
│ TicketCategory │ │ Subscription │
└────┬───────────┘ └──────┬───────┘
     │                    │
     │ 1:N                │ 1:N
     ▼                    ▼
┌─────────────────────┐  
│ TicketReservation   │  
└──────┬──────────────┘  
       │ 1:N
       ▼
┌──────────┐
│  Ticket  │
└──────────┘
```

---

## Event Management

### event

**Purpose**: Stores event information (conferences, concerts, etc.)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| short_name | VARCHAR(2048) | Unique URL-friendly name |
| display_name | VARCHAR(255) | Display name for UI |
| format | event_format | IN_PERSON, ONLINE, HYBRID |
| location | VARCHAR(2048) | Physical location |
| latitude | VARCHAR(255) | GPS latitude |
| longitude | VARCHAR(255) | GPS longitude |
| start_ts | TIMESTAMPTZ | Event start date/time |
| end_ts | TIMESTAMPTZ | Event end date/time |
| time_zone | VARCHAR(255) | Event timezone |
| currency | VARCHAR(3) | ISO currency code (e.g., USD, EUR) |
| available_seats | INTEGER | Total capacity |
| vat_included | BOOLEAN | Is VAT included in price |
| vat | DECIMAL(5,2) | VAT percentage |
| allowed_payment_proxies | VARCHAR(2048) | Payment methods (JSON array) |
| org_id | INTEGER | FK to organization |
| status | VARCHAR(255) | DRAFT, PUBLIC, DISABLED |
| metadata | JSONB | Additional event metadata |

**Constraints**:
- UNIQUE(short_name)
- UNIQUE(private_key)
- FK(org_id) → organization(id)

**Indexes**:
- PRIMARY KEY (id)
- INDEX (org_id)
- INDEX (short_name)
- INDEX (status)

---

### ticket_category

**Purpose**: Ticket tiers/types within an event (e.g., VIP, General Admission)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| event_id | INTEGER | FK to event |
| name | VARCHAR(255) | Category name |
| description | VARCHAR(1024) | Description |
| inception | TIMESTAMPTZ | Sale start date/time |
| expiration | TIMESTAMPTZ | Sale end date/time |
| max_tickets | INTEGER | Maximum tickets in category |
| price_cts | INTEGER | Price in cents |
| access_restricted | BOOLEAN | Requires special code |
| bounded | BOOLEAN | Fixed capacity vs unbounded |
| tc_status | VARCHAR(255) | ACTIVE, INACTIVE |
| ordinal | INTEGER | Display order |
| ticket_access_type | ticket_access_type | IN_PERSON, ONLINE, INHERIT |

**Constraints**:
- FK(event_id) → event(id)
- CHECK (expiration > inception)

**Business Rules**:
- Bounded categories have fixed capacity
- Unbounded categories share event's available pool
- Access-restricted categories require promo code

---

### additional_service

**Purpose**: Add-on services (parking, merchandise, workshops)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| event_id_fk | INTEGER | FK to event |
| price_cts | INTEGER | Price in cents |
| fix_price | BOOLEAN | Fixed vs quantity-based pricing |
| ordinal | INTEGER | Display order |
| available_quantity | INTEGER | Available items |
| max_qty_per_order | INTEGER | Max per order |
| inception_ts | TIMESTAMPTZ | Sale start |
| expiration_ts | TIMESTAMPTZ | Sale end |
| vat | DECIMAL(5,2) | VAT percentage |
| vat_type | service_vat_type | INHERITED, NONE, CUSTOM |
| as_type | service_type | DONATION, SUPPLEMENT |
| supplement_policy | supplement_policy | MANDATORY_ONE_FOR_TICKET, OPTIONAL_MAX_AMOUNT_PER_TICKET |

---

## Ticket & Reservation

### tickets_reservation

**Purpose**: Holds ticket reservations (cart + checkout)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | CHAR(36) | UUID primary key |
| validity | TIMESTAMPTZ | Expiration timestamp |
| status | reservation_status | PENDING, IN_PAYMENT, COMPLETE, etc. |
| full_name | VARCHAR(255) | Customer name |
| first_name | VARCHAR(255) | First name |
| last_name | VARCHAR(255) | Last name |
| email_address | VARCHAR(255) | Customer email |
| billing_address_company | VARCHAR(255) | Company name |
| billing_address_line1 | VARCHAR(255) | Address line 1 |
| billing_address_line2 | VARCHAR(255) | Address line 2 |
| billing_address_zip | VARCHAR(50) | Postal code |
| billing_address_city | VARCHAR(255) | City |
| billing_address_state | VARCHAR(255) | State/province |
| vat_nr | VARCHAR(255) | VAT/Tax ID |
| vat_country | VARCHAR(255) | VAT country code |
| invoice_number | VARCHAR(255) | Generated invoice number |
| invoice_requested | BOOLEAN | Invoice generation requested |
| user_language | VARCHAR(255) | User's language preference |
| event_id_fk | INTEGER | FK to event |
| promo_code_id_fk | INTEGER | FK to promo_code |
| src_price_cts | INTEGER | Original price (cents) |
| final_price_cts | INTEGER | Final price after discounts |
| vat_cts | INTEGER | VAT amount (cents) |
| discount_cts | INTEGER | Discount amount (cents) |
| currency_code | VARCHAR(3) | Currency |
| confirmation_ts | TIMESTAMPTZ | Confirmation timestamp |
| latest_reminder_ts | TIMESTAMPTZ | Last reminder sent |
| metadata | JSONB | Additional reservation data |

**Status Flow**:
```
PENDING → IN_PAYMENT → COMPLETE
   ↓           ↓
CANCELLED  CANCELLED
```

**Constraints**:
- FK(event_id_fk) → event(id)
- FK(promo_code_id_fk) → promo_code(id)

**Indexes**:
- PRIMARY KEY (id)
- INDEX (event_id_fk)
- INDEX (status)
- INDEX (email_address)
- PARTIAL INDEX (event_id_fk, status) WHERE status IN ('PENDING', 'OFFLINE_PAYMENT', 'DEFERRED_OFFLINE_PAYMENT')

---

### ticket

**Purpose**: Individual tickets within a reservation

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| uuid | CHAR(36) | Public ticket UUID |
| public_uuid | UUID | Public-facing UUID (QR code) |
| creation | TIMESTAMPTZ | Creation timestamp |
| category_id | INTEGER | FK to ticket_category |
| event_id | INTEGER | FK to event |
| status | ticket_status | FREE, PENDING, ACQUIRED, CHECKED_IN, etc. |
| original_price_cts | INTEGER | Original price (cents) |
| paid_price_cts | INTEGER | Actually paid price (cents) |
| vat_cts | INTEGER | VAT amount (cents) |
| discount_cts | INTEGER | Discount amount (cents) |
| tickets_reservation_id | CHAR(36) | FK to tickets_reservation |
| full_name | VARCHAR(255) | Attendee full name |
| first_name | VARCHAR(255) | Attendee first name |
| last_name | VARCHAR(255) | Attendee last name |
| email_address | VARCHAR(255) | Attendee email |
| locked_assignment | BOOLEAN | Assignment locked |
| user_language | VARCHAR(255) | Attendee language |
| ext_reference | VARCHAR(255) | External reference |
| src_price_cts | INTEGER | Source price |
| final_price_cts | INTEGER | Final price |
| vat_status | price_vat_status | INHERITED, NOT_INCLUDED_NOT_CHARGED, etc. |
| job_title | VARCHAR(255) | Attendee job title (custom field) |
| company | VARCHAR(255) | Attendee company (custom field) |
| phone_number | VARCHAR(255) | Attendee phone |
| notes | TEXT | Internal notes |
| special_price_id_fk | INTEGER | FK to special_price |
| metadata | JSONB | Additional ticket metadata |

**Status Values**:
- `FREE` - Available for purchase
- `PENDING` - In reservation, not paid
- `ACQUIRED` - Paid, not checked in
- `TO_BE_PAID` - Offline payment pending
- `CHECKED_IN` - Used at event
- `CANCELLED` - Cancelled
- `RELEASED` - Released back to pool
- `INVALIDATED` - Invalidated

**Constraints**:
- UNIQUE(uuid)
- UNIQUE(public_uuid)
- FK(category_id) → ticket_category(id)
- FK(event_id) → event(id)
- FK(tickets_reservation_id) → tickets_reservation(id)

**Indexes**:
- PRIMARY KEY (id)
- INDEX (uuid)
- INDEX (public_uuid)
- INDEX (tickets_reservation_id)
- INDEX (event_id, status)
- INDEX (category_id)

---

### additional_service_item

**Purpose**: Links additional services to reservations

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| tickets_reservation_uuid | CHAR(36) | FK to tickets_reservation |
| additional_service_id_fk | INTEGER | FK to additional_service |
| ticket_id_fk | INTEGER | FK to ticket (optional, for ticket-specific items) |
| amount | INTEGER | Quantity |
| src_price_cts | INTEGER | Source price per item |
| final_price_cts | INTEGER | Final price per item |
| vat_cts | INTEGER | VAT per item |
| discount_cts | INTEGER | Discount per item |
| ordinal | INTEGER | Display order |

---

## Subscription

### subscription_descriptor

**Purpose**: Defines subscription plans (passes, memberships)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| title | JSONB | Localized titles |
| description | JSONB | Localized descriptions |
| max_available | INTEGER | Maximum subscriptions |
| on_sale_from | TIMESTAMPTZ | Sale start |
| on_sale_to | TIMESTAMPTZ | Sale end |
| price_cts | INTEGER | Price in cents |
| vat | DECIMAL(5,2) | VAT percentage |
| vat_status | price_vat_status | VAT handling |
| currency | VARCHAR(3) | Currency code |
| is_public | BOOLEAN | Publicly visible |
| organization_id_fk | INTEGER | FK to organization |
| max_entries | INTEGER | Max uses per subscription |
| validity_type | subscription_validity_type | STANDARD, CUSTOM, NOT_SET |
| validity_time_unit | subscription_time_unit | DAYS, MONTHS, YEARS |
| validity_units | INTEGER | Validity duration |
| validity_from | TIMESTAMPTZ | Valid from |
| validity_to | TIMESTAMPTZ | Valid to |
| usage_type | subscription_usage_type | ONCE_PER_EVENT, UNLIMITED |
| status | subscription_status | ACTIVE, NOT_ACTIVE |
| terms_conditions_url | VARCHAR(2048) | Terms URL |
| privacy_policy_url | VARCHAR(2048) | Privacy policy URL |
| file_blob_id_fk | VARCHAR(255) | File attachment |
| allowed_payment_proxies | TEXT[] | Payment methods array |
| time_zone | VARCHAR(255) | Timezone |
| supports_tickets_generation | BOOLEAN | Can generate tickets |
| metadata | JSONB | Additional metadata |

---

### subscription

**Purpose**: Individual subscription instances

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| subscription_descriptor_id_fk | UUID | FK to subscription_descriptor |
| reservation_id_fk | CHAR(36) | FK to tickets_reservation |
| max_usage | INTEGER | Usage limit |
| valid_from | TIMESTAMPTZ | Valid from |
| valid_to | TIMESTAMPTZ | Valid to |
| src_price_cts | INTEGER | Source price |
| currency | VARCHAR(3) | Currency |
| organization_id_fk | INTEGER | FK to organization |
| status | subscription_status | Status |
| max_entries | INTEGER | Max entries |
| time_zone | VARCHAR(255) | Timezone |
| email_address | VARCHAR(255) | Subscriber email |
| first_name | VARCHAR(255) | Subscriber first name |
| last_name | VARCHAR(255) | Subscriber last name |
| company | VARCHAR(255) | Company |
| billing_address_line1 | VARCHAR(255) | Address |
| billing_address_zip | VARCHAR(255) | Postal code |
| billing_address_city | VARCHAR(255) | City |
| vat_nr | VARCHAR(255) | VAT number |
| vat_country | VARCHAR(255) | VAT country |
| invoice_number | VARCHAR(255) | Invoice number |
| confirmation_ts | TIMESTAMPTZ | Confirmation time |
| metadata | JSONB | Additional metadata |

---

### subscription_event

**Purpose**: Links subscriptions to events

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| event_id_fk | INTEGER | FK to event |
| subscription_descriptor_id_fk | UUID | FK to subscription_descriptor |
| price_per_ticket | INTEGER | Override ticket price |
| organization_id_fk | INTEGER | FK to organization |
| compatible_categories | INTEGER[] | Compatible category IDs |

---

## Billing & Payment

### b_transaction

**Purpose**: Payment transactions

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| gtw_tx_id | VARCHAR(2048) | Gateway transaction ID |
| gtw_payment_id | VARCHAR(2048) | Gateway payment ID |
| reservation_id | CHAR(36) | FK to tickets_reservation |
| t_timestamp | TIMESTAMPTZ | Transaction timestamp |
| price_cts | INTEGER | Amount in cents |
| currency | VARCHAR(255) | Currency code |
| description | VARCHAR(2048) | Description |
| payment_proxy | VARCHAR(2048) | Payment method (STRIPE, PAYPAL, etc.) |
| plat_fee | BIGINT | Platform fee |
| gtw_fee | BIGINT | Gateway fee |
| status | transaction_status | PENDING, COMPLETE, FAILED, CANCELLED |
| metadata | JSONB | Transaction metadata |

---

### billing_document

**Purpose**: Generated invoices and receipts

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| event_id_fk | INTEGER | FK to event (nullable) |
| reservation_id_fk | CHAR(36) | FK to tickets_reservation |
| subscription_id_fk | UUID | FK to subscription (nullable) |
| number | VARCHAR(255) | Document number |
| type | billing_document_type | INVOICE, RECEIPT, CREDIT_NOTE |
| model | TEXT | Document data (JSON) |
| generation_ts | TIMESTAMPTZ | Generation timestamp |
| status | billing_document_status | VALID, NOT_VALID |

**Document Types**:
- `INVOICE` - Tax invoice
- `RECEIPT` - Payment receipt
- `CREDIT_NOTE` - Refund/correction note

---

## User & Organization

### organization

**Purpose**: Multi-tenant organizations (event organizers)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| name | VARCHAR(255) | Organization name |
| description | VARCHAR(2048) | Description |
| email | VARCHAR(2048) | Contact email |
| name_openid | VARCHAR(2048) | External identity provider ID |
| slug | VARCHAR(255) | URL-friendly slug |

**Constraints**:
- UNIQUE(name)
- UNIQUE(slug)

---

### ba_user

**Purpose**: System users (admins, operators)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| username | VARCHAR(255) | Username |
| password | VARCHAR(2048) | Hashed password |
| first_name | VARCHAR(255) | First name |
| last_name | VARCHAR(255) | Last name |
| email_address | VARCHAR(255) | Email |
| enabled | BOOLEAN | Account enabled |
| user_type | user_type | INTERNAL, DEMO, API_KEY, PUBLIC |
| creation_time | TIMESTAMPTZ | Creation timestamp |
| valid_to_ts | TIMESTAMPTZ | Expiration (API keys) |

**Constraints**:
- UNIQUE(username)

---

### authority

**Purpose**: User roles and permissions

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| username | VARCHAR(255) | FK to ba_user.username |
| role | VARCHAR(255) | Role name |

**Standard Roles**:
- `ROLE_ADMIN` - System administrator
- `ROLE_OWNER` - Organization owner
- `ROLE_OPERATOR` - Event operator
- `ROLE_SPONSOR` - Sponsor (limited access)
- `ROLE_API_CONSUMER` - API access

---

### a_group

**Purpose**: User groups for access control

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| name | VARCHAR(255) | Group name |
| description | VARCHAR(1024) | Description |
| organization_id_fk | INTEGER | FK to organization |
| active | BOOLEAN | Active status |

---

### group_member

**Purpose**: Group membership

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| group_id_fk | INTEGER | FK to a_group |
| value | VARCHAR(255) | Member identifier (email, code) |
| description | VARCHAR(1024) | Description |

---

### group_link

**Purpose**: Links groups to events/categories

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| group_id_fk | INTEGER | FK to a_group |
| event_id_fk | INTEGER | FK to event |
| ticket_category_id_fk | INTEGER | FK to ticket_category (optional) |
| type | group_link_type | ONCE, LIMITED, UNLIMITED |
| match_type | match_type | FULL, PARTIAL |
| max_allocation | INTEGER | Max tickets per member |
| organization_id_fk | INTEGER | FK to organization |

---

## Audit & Logging

### auditing

**Purpose**: Comprehensive audit trail

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| reservation_id | CHAR(36) | Reservation ID |
| user_id | INTEGER | User ID who made change |
| event_id | INTEGER | Event ID |
| event_type | audit_event_type | Type of event |
| event_time | TIMESTAMPTZ | When it occurred |
| entity_type | entity_type | What was changed |
| entity_id | VARCHAR(255) | ID of changed entity |
| modifications | TEXT | Changes (JSON) |

**Event Types**:
- `RESERVATION_CREATE`, `RESERVATION_COMPLETE`, `RESERVATION_CANCEL`
- `TICKET_ASSIGN`, `TICKET_CHECK_IN`, `TICKET_RELEASE`
- `UPDATE_EVENT`, `UPDATE_TICKET_CATEGORY`
- `PAYMENT_CONFIRMED`, `PAYMENT_FAILED`

**Entity Types**:
- `EVENT`, `TICKET`, `RESERVATION`, `SUBSCRIPTION`

---

### scan_audit

**Purpose**: Check-in scan audit trail

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| ticket_uuid | VARCHAR(255) | Ticket UUID scanned |
| event_id_fk | INTEGER | FK to event |
| scan_ts | TIMESTAMPTZ | Scan timestamp |
| user_id | INTEGER | User who scanned |

---

## Configuration

### configuration

**Purpose**: System and event configuration

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| c_key | VARCHAR(255) | Configuration key |
| c_value | TEXT | Configuration value |
| description | VARCHAR(2048) | Description |

**Configuration Levels** (via purchase_context_configuration):
- **System** - Global settings
- **Organization** - Org-level settings
- **Event** - Event-specific settings
- **Category** - Category-specific settings

---

### purchase_context_configuration

**Purpose**: Hierarchical configuration (replaces old configuration table)

**Key Columns**:
| Column | Type | Description |
|--------|------|-------------|
| event_id_fk | INTEGER | Event ID (nullable) |
| subscription_descriptor_id_fk | UUID | Subscription ID (nullable) |
| ticket_category_id_fk | INTEGER | Category ID (nullable) |
| organization_id_fk | INTEGER | Organization ID (nullable) |
| c_key | VARCHAR(255) | Config key |
| c_value | TEXT | Config value |
| description | VARCHAR(2048) | Description |

**Configuration Hierarchy** (most specific wins):
```
Category → Event/Subscription → Organization → System
```

---

## Views

### events_statistics

**Purpose**: Real-time event statistics

**Columns**:
- `id` - Event ID
- `available_seats` - Total capacity
- `allocated_count` - Tickets allocated
- `sold_tickets_count` - Tickets sold
- `checked_in_count` - Tickets checked in
- `pending_count` - Pending tickets
- `dynamic_allocation` - Dynamic capacity
- `is_containing_orphan_tickets` - Has orphaned tickets
- `is_containing_stuck_tickets_count` - Has stuck tickets

---

### ticket_category_statistics

**Purpose**: Category-level statistics

**Columns**:
- `ticket_category_id` - Category ID
- `max_tickets` - Capacity
- `sold_tickets_count` - Sold
- `checked_in_count` - Checked in
- `pending_count` - Pending
- `not_sold_tickets` - Remaining
- `stuck_count` - Stuck reservations
- `is_containing_orphan_tickets` - Has orphans
- `is_containing_stuck_tickets` - Has stuck

---

### reservation_and_ticket_and_tx

**Purpose**: Complete reservation view with tickets and transactions

**Joins**:
- `tickets_reservation`
- `ticket`
- `b_transaction`
- `billing_document`

---

## Indexes

### Performance-Critical Indexes

**tickets_reservation**:
```sql
CREATE INDEX idx_reservation_status ON tickets_reservation(status);
CREATE INDEX idx_reservation_email ON tickets_reservation(email_address);
CREATE INDEX idx_reservation_event_status 
  ON tickets_reservation(event_id_fk, status) 
  WHERE status IN ('PENDING', 'OFFLINE_PAYMENT', 'DEFERRED_OFFLINE_PAYMENT');
```

**ticket**:
```sql
CREATE UNIQUE INDEX idx_ticket_uuid ON ticket(uuid);
CREATE INDEX idx_ticket_reservation ON ticket(tickets_reservation_id);
CREATE INDEX idx_ticket_event_status ON ticket(event_id, status);
CREATE INDEX idx_ticket_category ON ticket(category_id);
CREATE INDEX idx_ticket_email ON ticket(email_address);
```

**event**:
```sql
CREATE UNIQUE INDEX idx_event_short_name ON event(short_name);
CREATE INDEX idx_event_org ON event(org_id);
CREATE INDEX idx_event_status ON event(status);
```

---

## Constraints

### Foreign Key Constraints

**Cascade Behavior**:
- Most FKs: `ON DELETE RESTRICT` (prevent deletion if referenced)
- Some join tables: `ON DELETE CASCADE` (cascade delete)

### Check Constraints

**date_ranges**:
```sql
ALTER TABLE event ADD CONSTRAINT check_event_dates 
  CHECK (end_ts > start_ts);

ALTER TABLE ticket_category ADD CONSTRAINT check_category_dates 
  CHECK (expiration > inception);
```

**price_validation**:
```sql
ALTER TABLE ticket ADD CONSTRAINT check_ticket_prices 
  CHECK (paid_price_cts >= 0);
```

---

## Row-Level Security

### RLS Policies

**Enabled on**:
- `event`
- `ticket`
- `tickets_reservation`
- `ticket_category`
- `additional_service`

**Policy Logic**:
```sql
-- Users can only see events in their organizations
CREATE POLICY event_organization_policy ON event
  FOR ALL
  TO authenticated_user
  USING (org_id IN (SELECT org_id FROM user_organization WHERE user_id = current_user_id()));
```

**Bypass for Superusers**:
```sql
ALTER TABLE event FORCE ROW LEVEL SECURITY;
-- Superusers bypass RLS automatically
```

---

## Migration Strategy

### Flyway Migration Files

**Naming Convention**:
```
V{version}__{description}.sql
  V1__INITIAL_VERSION.sql
  V202_2.0.0.14__ADD_CATEGORY_ORDINAL.sql
  
afterMigrateApplied__{sequence}_{description}.sql
  afterMigrateApplied__004_VIEW_events_statistics.sql
```

**Version Numbers**:
- V1-V22: Version 1.x
- V23-V199: Reserved
- V200+: Version 2.0+
- V202: Version 2.0.0.x
- V203: Version 2.0.0.2x-3x
- V204: Version 2.0.0.3x-4x
- V205: Version 2.0.0.5x
- V206: Version 2.0.0.6x

**After-Migration Scripts**:
- Run after all versioned migrations
- Recreate views (drop/create)
- Update statistics
- Refresh materialized views

---

## Database Functions

### jsonb_merge_recurse

**Purpose**: Deep merge JSONB objects

**Usage**:
```sql
UPDATE event 
SET metadata = jsonb_merge_recurse(metadata, '{"key": "value"}'::jsonb) 
WHERE id = 1;
```

---

## Best Practices

### Query Optimization

1. **Use views for complex joins**
   - `events_statistics` instead of manual aggregation
   - `reservation_and_ticket_and_tx` for full reservation data

2. **Leverage indexes**
   - Status-based queries use status indexes
   - Date range queries use timestamp indexes

3. **Batch operations**
   - Use `UPDATE ... WHERE id IN (...)` for bulk updates
   - Avoid row-by-row updates in loops

4. **Connection pooling**
   - Use HikariCP (already configured)
   - Max pool size: 10 connections (default)

### Data Integrity

1. **Transactions**
   - Wrap multi-table operations in transactions
   - Use `@Transactional` annotation

2. **Optimistic locking**
   - Event has `version` column
   - Check version on update to prevent conflicts

3. **Audit everything**
   - Use `AuditingRepository` for state changes
   - Capture before/after values in modifications

---

## Backup & Recovery

### Recommended Strategy

**Backup**:
```bash
pg_dump -Fc -f alfio_backup.dump -d alfio
```

**Restore**:
```bash
pg_restore -d alfio_new alfio_backup.dump
```

**Point-in-Time Recovery**:
- Enable WAL archiving
- Configure continuous archiving
- Maintain transaction logs

---

## Database Sizing

### Estimated Storage

**Per 1000 Events**:
- Events: ~500 KB
- Ticket Categories: ~200 KB
- Reservations: ~5 MB
- Tickets: ~10 MB
- Transactions: ~2 MB
- Audit logs: ~20 MB

**Total per 1000 Events**: ~37.7 MB

**Indexes**: ~30% overhead → ~49 MB total

---

## Related Documentation

- **[DDD Mapping](./DDD_MAPPING.md)** - Domain model
- **[API Documentation](../README.md)** - API reference
- **Database Migrations**: `src/main/resources/alfio/db/PGSQL/`

---

**Documentation Version**: 1.0  
**Last Updated**: February 12, 2026  
**Database Schema Version**: 2.0.0.60+

