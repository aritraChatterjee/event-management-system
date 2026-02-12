# Database ER Diagrams - Alf.io

This document contains Entity-Relationship diagrams for the Alf.io database schema using Mermaid notation.

---

## Complete Schema Overview

```mermaid
erDiagram
    ORGANIZATION ||--o{ EVENT : owns
    ORGANIZATION ||--o{ SUBSCRIPTION_DESCRIPTOR : owns
    ORGANIZATION ||--o{ BA_USER : employs
    ORGANIZATION ||--o{ A_GROUP : manages
    
    EVENT ||--o{ TICKET_CATEGORY : contains
    EVENT ||--o{ ADDITIONAL_SERVICE : offers
    EVENT ||--o{ TICKETS_RESERVATION : receives
    EVENT ||--o{ PROMO_CODE : provides
    EVENT ||--o{ POLL : hosts
    EVENT ||--o{ BILLING_DOCUMENT : generates
    
    TICKET_CATEGORY ||--o{ TICKET : categorizes
    TICKET_CATEGORY ||--o{ SPECIAL_PRICE : restricts
    TICKET_CATEGORY ||--o{ GROUP_LINK : limits
    
    TICKETS_RESERVATION ||--o{ TICKET : contains
    TICKETS_RESERVATION ||--o{ ADDITIONAL_SERVICE_ITEM : includes
    TICKETS_RESERVATION ||--o{ B_TRANSACTION : processes
    TICKETS_RESERVATION ||--o{ BILLING_DOCUMENT : invoices
    
    TICKET ||--o{ FIELD_VALUE : stores
    TICKET ||--o{ SCAN_AUDIT : tracks
    
    ADDITIONAL_SERVICE ||--o{ ADDITIONAL_SERVICE_ITEM : sold_as
    ADDITIONAL_SERVICE ||--o{ ADDITIONAL_SERVICE_TEXT : describes
    
    SUBSCRIPTION_DESCRIPTOR ||--o{ SUBSCRIPTION : instances
    SUBSCRIPTION_DESCRIPTOR ||--o{ SUBSCRIPTION_EVENT : links_to_events
    SUBSCRIPTION ||--o{ BILLING_DOCUMENT : generates_invoices
    
    BA_USER ||--o{ AUTHORITY : has_roles
    BA_USER }o--|| J_USER_ORGANIZATION : member_of
    J_USER_ORGANIZATION }o--|| ORGANIZATION : belongs_to
    
    A_GROUP ||--o{ GROUP_MEMBER : contains
    A_GROUP ||--o{ GROUP_LINK : connected_via
    
    PROMO_CODE ||--o{ TICKETS_RESERVATION : applies_to
    
    POLL ||--o{ POLL_OPTION : has_options
    POLL_OPTION ||--o{ POLL_PARTICIPANT : receives_votes
```

---

## Core Event Management

```mermaid
erDiagram
    ORGANIZATION {
        int id PK
        varchar name UK
        varchar description
        varchar email
        varchar slug UK
    }
    
    EVENT {
        int id PK
        varchar short_name UK
        varchar display_name
        event_format format
        varchar location
        varchar latitude
        varchar longitude
        timestamptz start_ts
        timestamptz end_ts
        varchar time_zone
        varchar currency
        int available_seats
        boolean vat_included
        decimal vat
        text allowed_payment_proxies
        varchar private_key UK
        int org_id FK
        varchar status
        jsonb metadata
    }
    
    TICKET_CATEGORY {
        int id PK
        int event_id FK
        varchar name
        text description
        timestamptz inception
        timestamptz expiration
        int max_tickets
        int price_cts
        boolean access_restricted
        boolean bounded
        varchar tc_status
        int ordinal
        ticket_access_type ticket_access_type
        jsonb metadata
    }
    
    ADDITIONAL_SERVICE {
        int id PK
        int event_id_fk FK
        int price_cts
        boolean fix_price
        int ordinal
        int available_quantity
        int max_qty_per_order
        timestamptz inception_ts
        timestamptz expiration_ts
        decimal vat
        service_vat_type vat_type
        service_type as_type
        supplement_policy supplement_policy
    }
    
    ORGANIZATION ||--o{ EVENT : "owns"
    EVENT ||--o{ TICKET_CATEGORY : "contains"
    EVENT ||--o{ ADDITIONAL_SERVICE : "offers"
```

---

## Reservation & Ticketing

```mermaid
erDiagram
    TICKETS_RESERVATION {
        char(36) id PK
        timestamptz validity
        reservation_status status
        varchar full_name
        varchar first_name
        varchar last_name
        varchar email_address
        varchar billing_address_company
        varchar billing_address_line1
        varchar billing_address_zip
        varchar billing_address_city
        varchar vat_nr
        varchar vat_country
        varchar invoice_number
        int event_id_fk FK
        int promo_code_id_fk FK
        int src_price_cts
        int final_price_cts
        int vat_cts
        int discount_cts
        varchar currency_code
        timestamptz confirmation_ts
        jsonb metadata
    }
    
    TICKET {
        int id PK
        char(36) uuid UK
        uuid public_uuid UK
        timestamptz creation
        int category_id FK
        int event_id FK
        ticket_status status
        int original_price_cts
        int paid_price_cts
        int vat_cts
        int discount_cts
        char(36) tickets_reservation_id FK
        varchar full_name
        varchar first_name
        varchar last_name
        varchar email_address
        boolean locked_assignment
        varchar user_language
        int special_price_id_fk FK
        jsonb metadata
    }
    
    ADDITIONAL_SERVICE_ITEM {
        int id PK
        char(36) tickets_reservation_uuid FK
        int additional_service_id_fk FK
        int ticket_id_fk FK
        int amount
        int src_price_cts
        int final_price_cts
        int vat_cts
        int discount_cts
        int ordinal
    }
    
    SPECIAL_PRICE {
        int id PK
        varchar code UK
        int price_cts
        int ticket_category_id FK
        varchar status
        char(36) session_identifier
        timestamptz sent_timestamp
        varchar recipient_email
    }
    
    TICKETS_RESERVATION ||--o{ TICKET : "contains"
    TICKETS_RESERVATION ||--o{ ADDITIONAL_SERVICE_ITEM : "includes"
    TICKET }o--|| TICKET_CATEGORY : "belongs_to"
    TICKET }o--o| SPECIAL_PRICE : "uses"
    ADDITIONAL_SERVICE_ITEM }o--|| ADDITIONAL_SERVICE : "references"
    ADDITIONAL_SERVICE_ITEM }o--o| TICKET : "attached_to"
```

---

## Payment & Billing

```mermaid
erDiagram
    TICKETS_RESERVATION {
        char(36) id PK
        int final_price_cts
        varchar currency_code
        reservation_status status
    }
    
    B_TRANSACTION {
        int id PK
        varchar gtw_tx_id
        varchar gtw_payment_id
        char(36) reservation_id FK
        timestamptz t_timestamp
        int price_cts
        varchar currency
        varchar description
        varchar payment_proxy
        bigint plat_fee
        bigint gtw_fee
        transaction_status status
        jsonb metadata
    }
    
    BILLING_DOCUMENT {
        bigint id PK
        int event_id_fk FK
        char(36) reservation_id_fk FK
        uuid subscription_id_fk FK
        varchar number UK
        billing_document_type type
        text model
        timestamptz generation_ts
        billing_document_status status
    }
    
    INVOICE_SEQUENCES {
        int id PK
        int organization_id_fk FK
        varchar name
        int increment_value
    }
    
    TICKETS_RESERVATION ||--o{ B_TRANSACTION : "processes"
    TICKETS_RESERVATION ||--o{ BILLING_DOCUMENT : "generates"
    ORGANIZATION ||--o{ INVOICE_SEQUENCES : "manages"
```

---

## Subscription Management

```mermaid
erDiagram
    SUBSCRIPTION_DESCRIPTOR {
        uuid id PK
        jsonb title
        jsonb description
        int max_available
        timestamptz on_sale_from
        timestamptz on_sale_to
        int price_cts
        decimal vat
        price_vat_status vat_status
        varchar currency
        boolean is_public
        int organization_id_fk FK
        int max_entries
        subscription_validity_type validity_type
        subscription_time_unit validity_time_unit
        int validity_units
        timestamptz validity_from
        timestamptz validity_to
        subscription_usage_type usage_type
        subscription_status status
        text[] allowed_payment_proxies
        varchar time_zone
        jsonb metadata
    }
    
    SUBSCRIPTION {
        uuid id PK
        uuid subscription_descriptor_id_fk FK
        char(36) reservation_id_fk FK
        int max_usage
        timestamptz valid_from
        timestamptz valid_to
        int src_price_cts
        varchar currency
        int organization_id_fk FK
        subscription_status status
        varchar email_address
        varchar first_name
        varchar last_name
        varchar invoice_number
        timestamptz confirmation_ts
        jsonb metadata
    }
    
    SUBSCRIPTION_EVENT {
        int event_id_fk FK
        uuid subscription_descriptor_id_fk FK
        int price_per_ticket
        int organization_id_fk FK
        int[] compatible_categories
    }
    
    ORGANIZATION ||--o{ SUBSCRIPTION_DESCRIPTOR : "offers"
    SUBSCRIPTION_DESCRIPTOR ||--o{ SUBSCRIPTION : "instances"
    SUBSCRIPTION_DESCRIPTOR ||--o{ SUBSCRIPTION_EVENT : "linked_to"
    SUBSCRIPTION_EVENT }o--|| EVENT : "grants_access"
    SUBSCRIPTION }o--o| TICKETS_RESERVATION : "purchased_via"
```

---

## User & Access Control

```mermaid
erDiagram
    ORGANIZATION {
        int id PK
        varchar name
        varchar slug
    }
    
    BA_USER {
        int id PK
        varchar username UK
        varchar password
        varchar first_name
        varchar last_name
        varchar email_address
        boolean enabled
        user_type user_type
        timestamptz creation_time
        timestamptz valid_to_ts
    }
    
    AUTHORITY {
        varchar username FK
        varchar role
    }
    
    J_USER_ORGANIZATION {
        int user_id FK
        int org_id FK
    }
    
    A_GROUP {
        int id PK
        varchar name
        varchar description
        int organization_id_fk FK
        boolean active
    }
    
    GROUP_MEMBER {
        int id PK
        int group_id_fk FK
        varchar value
        varchar description
    }
    
    GROUP_LINK {
        int id PK
        int group_id_fk FK
        int event_id_fk FK
        int ticket_category_id_fk FK
        group_link_type type
        match_type match_type
        int max_allocation
        int organization_id_fk FK
    }
    
    BA_USER ||--o{ AUTHORITY : "has"
    BA_USER ||--o{ J_USER_ORGANIZATION : "member"
    J_USER_ORGANIZATION }o--|| ORGANIZATION : "of"
    ORGANIZATION ||--o{ A_GROUP : "manages"
    A_GROUP ||--o{ GROUP_MEMBER : "contains"
    A_GROUP ||--o{ GROUP_LINK : "grants_access"
    GROUP_LINK }o--o| EVENT : "for"
    GROUP_LINK }o--o| TICKET_CATEGORY : "to"
```

---

## Promo Codes & Special Pricing

```mermaid
erDiagram
    PROMO_CODE {
        int id PK
        varchar promo_code UK
        int event_id_fk FK
        int organization_id_fk FK
        timestamptz valid_from
        timestamptz valid_to
        int discount_amount
        discount_type discount_type
        varchar categories
        int max_usage
        varchar description
        varchar email_reference
        code_type code_type
        int hidden_category_id FK
        varchar currency_code
    }
    
    SPECIAL_PRICE {
        int id PK
        varchar code UK
        int price_cts
        int ticket_category_id FK
        varchar status
        varchar recipient_email
        timestamptz sent_timestamp
    }
    
    EVENT ||--o{ PROMO_CODE : "offers"
    ORGANIZATION ||--o{ PROMO_CODE : "creates"
    PROMO_CODE }o--o| TICKET_CATEGORY : "hides"
    TICKET_CATEGORY ||--o{ SPECIAL_PRICE : "restricts"
    TICKETS_RESERVATION }o--o| PROMO_CODE : "uses"
```

---

## Audit & Tracking

```mermaid
erDiagram
    AUDITING {
        char(36) reservation_id
        int user_id
        int event_id
        audit_event_type event_type
        timestamptz event_time
        entity_type entity_type
        varchar entity_id
        text modifications
    }
    
    SCAN_AUDIT {
        bigint id PK
        varchar ticket_uuid
        int event_id_fk FK
        timestamptz scan_ts
        int user_id FK
        varchar notes
        int ticket_id_fk FK
        varchar lead_status
    }
    
    EMAIL_MESSAGE {
        int id PK
        int event_id FK
        varchar status
        varchar recipient
        varchar subject
        text message
        text html_message
        text attachments
        varchar checksum UK
        timestamptz request_ts
        timestamptz sent_ts
        int attempts
        char(36) reservation_id
    }
    
    EVENT ||--o{ AUDITING : "tracks"
    EVENT ||--o{ SCAN_AUDIT : "records"
    EVENT ||--o{ EMAIL_MESSAGE : "sends"
    TICKET ||--o{ SCAN_AUDIT : "scanned"
```

---

## Configuration & Customization

```mermaid
erDiagram
    PURCHASE_CONTEXT_CONFIGURATION {
        int event_id_fk FK
        uuid subscription_descriptor_id_fk FK
        int ticket_category_id_fk FK
        int organization_id_fk FK
        varchar c_key
        text c_value
        varchar description
    }
    
    PURCHASE_CONTEXT_FIELD_CONFIGURATION {
        bigint id PK
        int event_id_fk FK
        uuid subscription_descriptor_id_fk FK
        varchar field_name
        int field_order
        field_type field_type
        boolean field_required
        jsonb field_restricted_values
        jsonb field_description
        int[] additional_service_ids
        int[] category_ids
    }
    
    PURCHASE_CONTEXT_FIELD_VALUE {
        bigint id PK
        bigint field_configuration_id_fk FK
        int ticket_id_fk FK
        uuid subscription_id_fk FK
        varchar field_value
    }
    
    EVENT ||--o{ PURCHASE_CONTEXT_CONFIGURATION : "configures"
    EVENT ||--o{ PURCHASE_CONTEXT_FIELD_CONFIGURATION : "defines_fields"
    PURCHASE_CONTEXT_FIELD_CONFIGURATION ||--o{ PURCHASE_CONTEXT_FIELD_VALUE : "stores"
    TICKET ||--o{ PURCHASE_CONTEXT_FIELD_VALUE : "has_values"
```

---

## Polls

```mermaid
erDiagram
    POLL {
        bigint id PK
        varchar title
        varchar description
        int event_id_fk FK
        poll_status status
        int order_
        timestamptz allowed_tags
    }
    
    POLL_OPTION {
        bigint id PK
        bigint poll_id_fk FK
        jsonb title
        jsonb description
    }
    
    POLL_PARTICIPANT {
        bigint id PK
        bigint poll_id_fk FK
        bigint poll_option_id_fk FK
        timestamptz poll_ts
        char(36) reservation_id_fk FK
    }
    
    EVENT ||--o{ POLL : "hosts"
    POLL ||--o{ POLL_OPTION : "has"
    POLL_OPTION ||--o{ POLL_PARTICIPANT : "receives"
    TICKETS_RESERVATION ||--o{ POLL_PARTICIPANT : "votes"
```

---

## File & Resource Management

```mermaid
erDiagram
    FILE_BLOB {
        varchar id PK
        varchar name
        bigint content_size
        varchar content_type
        bytea content
        jsonb attributes
    }
    
    RESOURCE_ORGANIZER {
        int organization_id_fk FK
        varchar name PK
        text content
        varchar content_type
    }
    
    RESOURCE_EVENT {
        int event_id_fk FK
        varchar name PK
        text content
        varchar content_type
    }
    
    RESOURCE_GLOBAL {
        varchar name PK
        text content
        varchar content_type
    }
    
    EVENT ||--o{ FILE_BLOB : "attaches"
    ORGANIZATION ||--o{ RESOURCE_ORGANIZER : "customizes"
    EVENT ||--o{ RESOURCE_EVENT : "customizes"
```

---

## Extension System

```mermaid
erDiagram
    EXTENSION_LOG {
        bigint id PK
        varchar path
        varchar name
        varchar hash
        boolean enabled
        timestamptz creation_ts
    }
    
    EXTENSION_EVENT {
        int id PK
        varchar path FK
        varchar event
        boolean active
    }
    
    EXTENSION_CONFIGURATION {
        bigint id PK
        varchar path FK
        varchar name
        varchar configuration_level
        int event_id_fk FK
        int organization_id_fk FK
        text configuration_value
    }
    
    EXTENSION_METADATA {
        varchar path PK
        jsonb metadata
    }
    
    EXTENSION_LOG ||--o{ EXTENSION_EVENT : "listens_to"
    EXTENSION_LOG ||--o{ EXTENSION_CONFIGURATION : "configures"
    EXTENSION_LOG ||--|| EXTENSION_METADATA : "describes"
```

---

## Admin Job Queue

```mermaid
erDiagram
    ADMIN_JOB_SCHEDULE {
        uuid id PK
        job_name job_name
        timestamptz schedule_ts
        jsonb metadata
        boolean execution_allowed
    }
    
    ADMIN_JOB_EXECUTION {
        uuid id PK
        uuid job_schedule_id_fk FK
        timestamptz start_ts
        timestamptz completion_ts
        job_execution_status status
        text result
    }
    
    ADMIN_JOB_SCHEDULE ||--o{ ADMIN_JOB_EXECUTION : "executes"
```

---

## Data Types (Enums)

### Event & Ticket Types

| Type | Values |
|------|--------|
| **event_format** | IN_PERSON, ONLINE, HYBRID |
| **ticket_status** | FREE, PENDING, ACQUIRED, TO_BE_PAID, CHECKED_IN, CANCELLED, RELEASED, INVALIDATED, EXPIRED |
| **reservation_status** | PENDING, IN_PAYMENT, EXTERNAL_PROCESSING_PAYMENT, OFFLINE_PAYMENT, DEFERRED_OFFLINE_PAYMENT, COMPLETE, STUCK, CANCELLED, CREDIT_NOTE_ISSUED |
| **ticket_access_type** | IN_PERSON, ONLINE, INHERIT |

### Billing Types

| Type | Values |
|------|--------|
| **billing_document_type** | INVOICE, RECEIPT, CREDIT_NOTE |
| **billing_document_status** | VALID, NOT_VALID |
| **transaction_status** | PENDING, COMPLETE, FAILED, CANCELLED |

### Subscription Types

| Type | Values |
|------|--------|
| **subscription_status** | ACTIVE, NOT_ACTIVE |
| **subscription_validity_type** | STANDARD, CUSTOM, NOT_SET |
| **subscription_time_unit** | DAYS, MONTHS, YEARS |
| **subscription_usage_type** | ONCE_PER_EVENT, UNLIMITED |

### Service Types

| Type | Values |
|------|--------|
| **service_type** | DONATION, SUPPLEMENT |
| **service_vat_type** | INHERITED, NONE, CUSTOM |
| **supplement_policy** | MANDATORY_ONE_FOR_TICKET, OPTIONAL_UNLIMITED_AMOUNT, OPTIONAL_MAX_AMOUNT_PER_TICKET, OPTIONAL_MAX_AMOUNT_PER_RESERVATION |

### Pricing Types

| Type | Values |
|------|--------|
| **price_vat_status** | INHERITED, NOT_INCLUDED_NOT_CHARGED, NOT_INCLUDED, INCLUDED, INCLUDED_EXEMPT, NONE, CUSTOM_INCLUDED, CUSTOM_EXCLUDED, CUSTOM_INCLUDED_EXEMPT |
| **discount_type** | PERCENTAGE, FIXED_AMOUNT, FIXED_AMOUNT_RESERVATION, NONE |
| **code_type** | DISCOUNT, ACCESS, DYNAMIC |

---

**Documentation Version**: 1.0  
**Last Updated**: February 12, 2026  
**Database Version**: 2.0.0.60+

