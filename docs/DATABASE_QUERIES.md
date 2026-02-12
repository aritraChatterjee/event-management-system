# Database Query Reference - Alf.io

Common database queries and patterns for the Alf.io event management system.

---

## Table of Contents

1. [Event Queries](#event-queries)
2. [Ticket & Reservation Queries](#ticket--reservation-queries)
3. [Subscription Queries](#subscription-queries)
4. [Reporting & Statistics](#reporting--statistics)
5. [User & Access Control](#user--access-control)
6. [Billing & Payment](#billing--payment)
7. [Audit Queries](#audit-queries)
8. [Performance Queries](#performance-queries)

---

## Event Queries

### Find All Active Events

```sql
SELECT * FROM event 
WHERE status = 'PUBLIC' 
  AND end_ts > CURRENT_TIMESTAMP
ORDER BY start_ts;
```

### Find Events by Organization

```sql
SELECT e.*, o.name as organization_name
FROM event e
JOIN organization o ON e.org_id = o.id
WHERE o.id = :organizationId
ORDER BY e.start_ts DESC;
```

### Get Event with Statistics

```sql
SELECT e.*, es.*
FROM event e
JOIN events_statistics es ON e.id = es.id
WHERE e.short_name = :shortName;
```

### Find Events by Date Range

```sql
SELECT * FROM event
WHERE start_ts >= :fromDate
  AND end_ts <= :toDate
  AND status = 'PUBLIC'
ORDER BY start_ts;
```

### Check Event Capacity

```sql
SELECT 
    e.available_seats,
    es.sold_tickets_count,
    es.checked_in_count,
    es.pending_count,
    (e.available_seats - es.sold_tickets_count - es.pending_count) as remaining_capacity
FROM event e
JOIN events_statistics es ON e.id = es.id
WHERE e.id = :eventId;
```

---

## Ticket & Reservation Queries

### Find Reservations by Status

```sql
SELECT * FROM tickets_reservation
WHERE event_id_fk = :eventId
  AND status = :status
ORDER BY validity DESC;
```

### Get Complete Reservation with Tickets

```sql
SELECT 
    tr.*,
    t.id as ticket_id,
    t.uuid as ticket_uuid,
    t.category_id,
    t.full_name as attendee_name,
    t.email_address as attendee_email,
    tc.name as category_name
FROM tickets_reservation tr
JOIN ticket t ON t.tickets_reservation_id = tr.id
JOIN ticket_category tc ON t.category_id = tc.id
WHERE tr.id = :reservationId
ORDER BY tc.ordinal, t.id;
```

### Find Expired Pending Reservations

```sql
SELECT * FROM tickets_reservation
WHERE status IN ('PENDING', 'OFFLINE_PAYMENT', 'DEFERRED_OFFLINE_PAYMENT')
  AND validity < CURRENT_TIMESTAMP
  AND event_id_fk = :eventId;
```

### Count Tickets by Status

```sql
SELECT 
    status,
    COUNT(*) as ticket_count
FROM ticket
WHERE event_id = :eventId
GROUP BY status
ORDER BY status;
```

### Find Tickets by Email

```sql
SELECT 
    t.*,
    e.short_name as event_name,
    e.display_name as event_display_name,
    tc.name as category_name,
    tr.status as reservation_status
FROM ticket t
JOIN event e ON t.event_id = e.id
JOIN ticket_category tc ON t.category_id = tc.id
LEFT JOIN tickets_reservation tr ON t.tickets_reservation_id = tr.id
WHERE LOWER(t.email_address) = LOWER(:email)
ORDER BY e.start_ts DESC;
```

### Get Tickets for Check-in

```sql
SELECT 
    t.id,
    t.uuid,
    t.public_uuid,
    t.full_name,
    t.email_address,
    t.status,
    tc.name as category_name,
    e.short_name as event_name
FROM ticket t
JOIN ticket_category tc ON t.category_id = tc.id
JOIN event e ON t.event_id = e.id
WHERE t.public_uuid = :publicUuid
  AND e.id = :eventId
  AND t.status IN ('ACQUIRED', 'TO_BE_PAID', 'CHECKED_IN')
FOR UPDATE;
```

### Find Orphaned Tickets

```sql
SELECT t.*
FROM ticket t
LEFT JOIN tickets_reservation tr ON t.tickets_reservation_id = tr.id
WHERE t.status = 'PENDING'
  AND (tr.id IS NULL OR tr.validity < CURRENT_TIMESTAMP)
  AND t.event_id = :eventId;
```

### Get Ticket with Custom Fields

```sql
SELECT 
    t.*,
    jsonb_object_agg(
        pcfc.field_name, 
        pcfv.field_value
    ) FILTER (WHERE pcfv.field_value IS NOT NULL) as custom_fields
FROM ticket t
LEFT JOIN purchase_context_field_value pcfv ON pcfv.ticket_id_fk = t.id
LEFT JOIN purchase_context_field_configuration pcfc ON pcfc.id = pcfv.field_configuration_id_fk
WHERE t.id = :ticketId
GROUP BY t.id;
```

---

## Subscription Queries

### Find Active Subscriptions for Organization

```sql
SELECT * FROM subscription_descriptor
WHERE organization_id_fk = :organizationId
  AND status = 'ACTIVE'
  AND (on_sale_to IS NULL OR on_sale_to > CURRENT_TIMESTAMP)
ORDER BY on_sale_from, on_sale_to NULLS LAST;
```

### Get Subscription with Usage

```sql
SELECT 
    s.*,
    sd.title,
    sd.max_entries,
    sd.usage_type,
    COUNT(t.id) as tickets_used,
    (sd.max_entries - COUNT(t.id)) as tickets_remaining
FROM subscription s
JOIN subscription_descriptor sd ON s.subscription_descriptor_id_fk = sd.id
LEFT JOIN ticket t ON t.subscription_id_fk = s.id
WHERE s.id = :subscriptionId
GROUP BY s.id, sd.id;
```

### Find Valid Subscriptions for User

```sql
SELECT s.*, sd.title, sd.description
FROM subscription s
JOIN subscription_descriptor sd ON s.subscription_descriptor_id_fk = sd.id
WHERE LOWER(s.email_address) = LOWER(:email)
  AND s.status = 'ACTIVE'
  AND s.valid_from <= CURRENT_TIMESTAMP
  AND s.valid_to >= CURRENT_TIMESTAMP
ORDER BY s.valid_to DESC;
```

### Check Subscription Compatibility with Event

```sql
SELECT se.*
FROM subscription_event se
JOIN subscription_descriptor sd ON se.subscription_descriptor_id_fk = sd.id
WHERE se.event_id_fk = :eventId
  AND sd.id = :subscriptionDescriptorId
  AND sd.status = 'ACTIVE';
```

---

## Reporting & Statistics

### Daily Sales Report

```sql
SELECT 
    DATE(tr.confirmation_ts) as sale_date,
    COUNT(DISTINCT tr.id) as reservations_count,
    COUNT(t.id) as tickets_sold,
    SUM(tr.final_price_cts) / 100.0 as total_revenue,
    SUM(tr.vat_cts) / 100.0 as total_vat
FROM tickets_reservation tr
JOIN ticket t ON t.tickets_reservation_id = tr.id
WHERE tr.event_id_fk = :eventId
  AND tr.status = 'COMPLETE'
  AND tr.confirmation_ts >= :fromDate
  AND tr.confirmation_ts < :toDate
GROUP BY DATE(tr.confirmation_ts)
ORDER BY sale_date;
```

### Revenue by Payment Method

```sql
SELECT 
    bt.payment_proxy,
    COUNT(DISTINCT tr.id) as transaction_count,
    SUM(bt.price_cts) / 100.0 as total_amount,
    SUM(bt.gtw_fee) / 100.0 as gateway_fees,
    SUM(bt.plat_fee) / 100.0 as platform_fees
FROM b_transaction bt
JOIN tickets_reservation tr ON bt.reservation_id = tr.id
WHERE tr.event_id_fk = :eventId
  AND bt.status = 'COMPLETE'
GROUP BY bt.payment_proxy
ORDER BY total_amount DESC;
```

### Category Sales Report

```sql
SELECT 
    tc.name as category_name,
    tcs.max_tickets,
    tcs.sold_tickets_count,
    tcs.checked_in_count,
    tcs.pending_count,
    tcs.not_sold_tickets as remaining,
    (tcs.sold_tickets_count * tc.price_cts / 100.0) as revenue
FROM ticket_category tc
JOIN ticket_category_statistics tcs ON tc.id = tcs.ticket_category_id
WHERE tc.event_id = :eventId
  AND tc.tc_status = 'ACTIVE'
ORDER BY tc.ordinal;
```

### Promo Code Usage Report

```sql
SELECT 
    pc.promo_code,
    pc.discount_type,
    pc.discount_amount,
    pc.max_usage,
    COUNT(DISTINCT tr.id) as times_used,
    COUNT(t.id) as tickets_with_discount,
    SUM(tr.discount_cts) / 100.0 as total_discount_amount
FROM promo_code pc
LEFT JOIN tickets_reservation tr ON tr.promo_code_id_fk = pc.id AND tr.status = 'COMPLETE'
LEFT JOIN ticket t ON t.tickets_reservation_id = tr.id
WHERE pc.event_id_fk = :eventId
GROUP BY pc.id
ORDER BY times_used DESC;
```

### Check-in Progress

```sql
SELECT 
    tc.name as category,
    COUNT(t.id) as total_tickets,
    COUNT(CASE WHEN t.status = 'CHECKED_IN' THEN 1 END) as checked_in,
    COUNT(CASE WHEN t.status = 'ACQUIRED' THEN 1 END) as not_checked_in,
    ROUND(
        100.0 * COUNT(CASE WHEN t.status = 'CHECKED_IN' THEN 1 END) / 
        NULLIF(COUNT(t.id), 0), 
        2
    ) as check_in_percentage
FROM ticket t
JOIN ticket_category tc ON t.category_id = tc.id
WHERE t.event_id = :eventId
  AND t.status IN ('ACQUIRED', 'CHECKED_IN')
GROUP BY tc.id, tc.name
ORDER BY tc.ordinal;
```

### Hourly Check-in Report

```sql
SELECT 
    DATE_TRUNC('hour', sa.scan_ts) as check_in_hour,
    COUNT(*) as check_ins
FROM scan_audit sa
WHERE sa.event_id_fk = :eventId
  AND sa.scan_ts >= :eventStartTime
  AND sa.scan_ts <= :eventEndTime
GROUP BY check_in_hour
ORDER BY check_in_hour;
```

---

## User & Access Control

### Find Users in Organization

```sql
SELECT 
    bu.*,
    STRING_AGG(DISTINCT a.role, ', ') as roles
FROM ba_user bu
JOIN j_user_organization juo ON bu.id = juo.user_id
LEFT JOIN authority a ON bu.username = a.username
WHERE juo.org_id = :organizationId
  AND bu.enabled = true
GROUP BY bu.id
ORDER BY bu.last_name, bu.first_name;
```

### Check User Permissions for Event

```sql
SELECT 
    EXISTS (
        SELECT 1 FROM event e
        JOIN j_user_organization juo ON e.org_id = juo.org_id
        WHERE e.id = :eventId
          AND juo.user_id = :userId
    ) as has_access;
```

### Find Groups with Access to Event

```sql
SELECT 
    g.id,
    g.name,
    g.description,
    gl.type as access_type,
    gl.max_allocation,
    tc.name as category_name
FROM a_group g
JOIN group_link gl ON g.id = gl.group_id_fk
LEFT JOIN ticket_category tc ON gl.ticket_category_id_fk = tc.id
WHERE gl.event_id_fk = :eventId
  AND g.active = true
ORDER BY g.name;
```

---

## Billing & Payment

### Find Unpaid Reservations

```sql
SELECT tr.*
FROM tickets_reservation tr
LEFT JOIN b_transaction bt ON tr.id = bt.reservation_id AND bt.status = 'COMPLETE'
WHERE tr.event_id_fk = :eventId
  AND tr.status IN ('PENDING', 'OFFLINE_PAYMENT')
  AND bt.id IS NULL
  AND tr.validity >= CURRENT_TIMESTAMP
ORDER BY tr.validity;
```

### Get Billing Documents for Reservation

```sql
SELECT * FROM billing_document
WHERE reservation_id_fk = :reservationId
  AND status = 'VALID'
ORDER BY generation_ts DESC;
```

### Find Failed Transactions

```sql
SELECT 
    bt.*,
    tr.email_address,
    tr.full_name,
    e.short_name as event_name
FROM b_transaction bt
JOIN tickets_reservation tr ON bt.reservation_id = tr.id
JOIN event e ON tr.event_id_fk = e.id
WHERE bt.status = 'FAILED'
  AND bt.t_timestamp >= :fromDate
ORDER BY bt.t_timestamp DESC;
```

### Invoice Sequence Status

```sql
SELECT 
    is_.*,
    o.name as organization_name,
    COUNT(bd.id) as invoices_generated
FROM invoice_sequences is_
JOIN organization o ON is_.organization_id_fk = o.id
LEFT JOIN billing_document bd ON bd.number LIKE (is_.name || '%')
WHERE is_.organization_id_fk = :organizationId
GROUP BY is_.id, o.name
ORDER BY is_.name;
```

---

## Audit Queries

### Get Audit Trail for Reservation

```sql
SELECT * FROM auditing_user
WHERE reservation_id = :reservationId
ORDER BY event_time ASC;
```

### Find Recent Check-ins

```sql
SELECT 
    sa.scan_ts,
    sa.ticket_uuid,
    t.full_name,
    t.email_address,
    tc.name as category,
    bu.username as scanned_by
FROM scan_audit sa
JOIN ticket t ON sa.ticket_uuid::uuid = t.public_uuid
JOIN ticket_category tc ON t.category_id = tc.id
LEFT JOIN ba_user bu ON sa.user_id = bu.id
WHERE sa.event_id_fk = :eventId
  AND sa.scan_ts >= :fromTime
ORDER BY sa.scan_ts DESC
LIMIT 100;
```

### Email Delivery Status

```sql
SELECT 
    status,
    COUNT(*) as message_count,
    COUNT(CASE WHEN sent_ts IS NOT NULL THEN 1 END) as sent_count,
    COUNT(CASE WHEN sent_ts IS NULL AND attempts > 0 THEN 1 END) as failed_count
FROM email_message
WHERE event_id = :eventId
  AND request_ts >= :fromDate
GROUP BY status;
```

---

## Performance Queries

### Find Slow Queries (PostgreSQL)

```sql
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements
WHERE query LIKE '%ticket%' OR query LIKE '%reservation%'
ORDER BY mean_time DESC
LIMIT 20;
```

### Table Size Report

```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
LIMIT 20;
```

### Index Usage Statistics

```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC
LIMIT 20;
```

### Find Missing Indexes

```sql
SELECT 
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    idx_scan,
    seq_tup_read / seq_scan as avg_seq_tup_read
FROM pg_stat_user_tables
WHERE seq_scan > 0
  AND schemaname = 'public'
ORDER BY seq_tup_read DESC
LIMIT 10;
```

---

## Maintenance Queries

### Vacuum and Analyze

```sql
-- Analyze specific table
ANALYZE ticket;

-- Vacuum specific table
VACUUM ANALYZE tickets_reservation;

-- Full vacuum (requires exclusive lock)
VACUUM FULL tickets_reservation;
```

### Reindex Table

```sql
REINDEX TABLE ticket;
REINDEX INDEX idx_ticket_uuid;
```

### Update Statistics

```sql
-- Update all statistics
ANALYZE;

-- Update statistics for event-related tables
ANALYZE event;
ANALYZE ticket;
ANALYZE tickets_reservation;
```

---

## Common Query Patterns

### Pagination

```sql
SELECT * FROM event
WHERE status = 'PUBLIC'
ORDER BY start_ts DESC
LIMIT :pageSize
OFFSET :offset;
```

### Full-Text Search on Events

```sql
SELECT * FROM event
WHERE 
    to_tsvector('english', display_name || ' ' || COALESCE(location, '')) 
    @@ to_tsquery('english', :searchTerm)
ORDER BY start_ts DESC;
```

### JSON Field Queries

```sql
-- Query event metadata
SELECT * FROM event
WHERE metadata->>'venue_capacity' = '1000';

-- Query nested JSON
SELECT * FROM event
WHERE metadata->'custom'->'field' = '"value"';

-- Check JSON key existence
SELECT * FROM event
WHERE metadata ? 'special_instructions';
```

### Conditional Aggregates

```sql
SELECT 
    event_id,
    COUNT(*) as total_tickets,
    COUNT(*) FILTER (WHERE status = 'CHECKED_IN') as checked_in,
    COUNT(*) FILTER (WHERE status = 'ACQUIRED') as acquired,
    COUNT(*) FILTER (WHERE status = 'PENDING') as pending
FROM ticket
WHERE event_id = :eventId
GROUP BY event_id;
```

### Window Functions for Rankings

```sql
SELECT 
    e.display_name,
    e.start_ts,
    es.sold_tickets_count,
    RANK() OVER (
        PARTITION BY e.org_id 
        ORDER BY es.sold_tickets_count DESC
    ) as sales_rank
FROM event e
JOIN events_statistics es ON e.id = es.id
WHERE e.org_id = :organizationId;
```

---

## Best Practices

### Use Prepared Statements

```java
// NPJT example
@Query("SELECT * FROM event WHERE id = :eventId")
Event findById(@Bind("eventId") int eventId);
```

### Avoid N+1 Queries

```sql
-- BAD: Multiple queries
SELECT * FROM event WHERE id = 1;
SELECT * FROM ticket_category WHERE event_id = 1;

-- GOOD: Single query with join
SELECT e.*, tc.*
FROM event e
LEFT JOIN ticket_category tc ON tc.event_id = e.id
WHERE e.id = 1;
```

### Use EXISTS Instead of COUNT

```sql
-- GOOD: Stop at first match
SELECT EXISTS (
    SELECT 1 FROM ticket 
    WHERE tickets_reservation_id = :reservationId
);

-- AVOID: Count all rows
SELECT COUNT(*) > 0 FROM ticket 
WHERE tickets_reservation_id = :reservationId;
```

### Batch Updates

```sql
-- Update multiple records in one query
UPDATE ticket
SET status = 'RELEASED'
WHERE id IN (:ticketIds);
```

---

**Query Reference Version**: 1.0  
**Last Updated**: February 12, 2026  
**Database Version**: PostgreSQL 10+

