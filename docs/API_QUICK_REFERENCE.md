# API Quick Reference - Alf.io

Quick reference for common API operations.

---

## Authentication

```bash
# HTTP Basic Auth
curl -u username:password https://your-domain.com/admin/api/events

# API Key
curl -H "Authorization: ApiKey your-api-key" https://your-domain.com/api/v1/admin/...
```

---

## Public API Quick Start

### Get Events List
```bash
curl https://your-domain.com/api/v2/public/events
```

### Get Event Details
```bash
curl https://your-domain.com/api/v2/public/event/conference-2024
```

### Create Reservation
```bash
curl -X POST https://your-domain.com/api/v2/public/event/conference-2024/reserve-tickets \
  -H "Content-Type: application/json" \
  -d '{
    "reservation": [
      {"ticketCategoryId": 1, "amount": 2}
    ],
    "promoCode": "EARLYBIRD"
  }'
```

### Get Reservation
```bash
curl https://your-domain.com/api/v2/public/event/conference-2024/reservation/abc-123-def
```

---

## Admin API Quick Start

### List All Events
```bash
curl -u admin:password https://your-domain.com/admin/api/events
```

### Get Event Stats
```bash
curl -u admin:password https://your-domain.com/admin/api/events/conference-2024
```

### List Reservations
```bash
curl -u admin:password \
  "https://your-domain.com/admin/api/reservation/event/conference-2024/reservations/all-status?page=0"
```

### Check In Ticket
```bash
curl -X POST -u admin:password \
  https://your-domain.com/admin/api/check-in/event/conference-2024/ticket/scan \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ticket-public-uuid",
    "scan_timestamp": 1710080400000
  }'
```

---

## Common Endpoints

### Public API (No Auth)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v2/public/events` | List events |
| GET | `/api/v2/public/event/{eventName}` | Event details |
| GET | `/api/v2/public/event/{eventName}/ticket-categories` | Available tickets |
| POST | `/api/v2/public/event/{eventName}/reserve-tickets` | Create reservation |
| GET | `/api/v2/public/event/{eventName}/reservation/{id}` | Get reservation |
| POST | `/api/v2/public/event/{eventName}/reservation/{id}` | Confirm payment |

### Admin API (Auth Required)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/admin/api/events` | List all events |
| GET | `/admin/api/events/{eventName}` | Event details |
| POST | `/admin/api/events/new` | Create event |
| DELETE | `/admin/api/events/{eventId}` | Delete event |
| GET | `/admin/api/reservation/{type}/{id}/reservations/all-status` | List reservations |
| POST | `/admin/api/check-in/event/{name}/ticket/{id}` | Check in ticket |
| GET | `/admin/api/events/{eventId}/promo-code` | List promo codes |
| POST | `/admin/api/events/{eventId}/promo-code` | Create promo code |

---

## Request Examples

### Create Event
```bash
curl -X POST -u admin:password \
  https://your-domain.com/admin/api/events/new \
  -H "Content-Type: application/json" \
  -d '{
    "shortName": "tech-conf-2024",
    "displayName": "Tech Conference 2024",
    "organizationId": 1,
    "location": "San Francisco",
    "startDate": {"date": "2024-06-15", "time": "09:00"},
    "endDate": {"date": "2024-06-17", "time": "18:00"},
    "timezone": "America/Los_Angeles",
    "currency": "USD",
    "availableSeats": 500,
    "regularPrice": 299.00,
    "vatIncluded": true,
    "vat": 8.5,
    "allowedPaymentProxies": ["STRIPE", "PAYPAL"]
  }'
```

### Create Promo Code
```bash
curl -X POST -u admin:password \
  https://your-domain.com/admin/api/events/1/promo-code \
  -H "Content-Type: application/json" \
  -d '{
    "promoCode": "EARLYBIRD",
    "discountType": "PERCENTAGE",
    "discountAmount": 20,
    "validFrom": {"date": "2024-01-01", "time": "00:00"},
    "validTo": {"date": "2024-03-31", "time": "23:59"},
    "maxUsage": 100,
    "codeType": "DISCOUNT"
  }'
```

### Manual Reservation
```bash
curl -X POST -u admin:password \
  https://your-domain.com/admin/api/reservation/event/conference-2024/new \
  -H "Content-Type: application/json" \
  -d '{
    "tickets": [{
      "categoryId": 1,
      "attendees": [{
        "firstName": "John",
        "lastName": "Doe",
        "emailAddress": "john@example.com"
      }]
    }],
    "customerData": {
      "firstName": "John",
      "lastName": "Doe",
      "emailAddress": "john@example.com"
    },
    "notification": {
      "customer": true,
      "attendees": false
    }
  }'
```

---

## Response Formats

### Success Response
```json
{
  "success": true,
  "value": "result-data",
  "errorCount": 0
}
```

### Error Response
```json
{
  "success": false,
  "errorCount": 2,
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format",
      "code": "error.email.invalid"
    },
    {
      "field": "category",
      "message": "Category not found",
      "code": "error.category.not.found"
    }
  ]
}
```

### Pagination Response
```json
{
  "page": 0,
  "pageSize": 50,
  "totalPages": 10,
  "totalElements": 487,
  "results": [...]
}
```

---

## Query Parameters

### Common Parameters
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `page` | int | Page number (0-based) | `?page=0` |
| `pageSize` | int | Items per page | `?pageSize=50` |
| `orderBy` | string | Sort field | `?orderBy=confirmationTimestamp` |
| `direction` | string | Sort direction | `?direction=DESC` |
| `search` | string | Search term | `?search=john@example.com` |
| `status` | string | Filter by status | `?status=COMPLETE` |

---

## Status Values

### Reservation Status
- `PENDING` - Awaiting payment
- `IN_PAYMENT` - Payment in progress
- `COMPLETE` - Confirmed and paid
- `CANCELLED` - Cancelled
- `OFFLINE_PAYMENT` - Awaiting offline payment
- `STUCK` - Error state

### Ticket Status
- `FREE` - Available
- `PENDING` - In reservation
- `ACQUIRED` - Paid
- `CHECKED_IN` - Used
- `CANCELLED` - Cancelled
- `RELEASED` - Returned to pool

---

## Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `error.email.required` | 400 | Email required |
| `error.notEnoughTickets` | 400 | Insufficient tickets |
| `error.promo.code.invalid` | 400 | Invalid promo code |
| `error.category.not.found` | 404 | Category not found |
| `error.reservation.expired` | 410 | Reservation expired |
| `error.payment.failed` | 402 | Payment failed |
| `error.unauthorized` | 401 | Auth required |
| `error.forbidden` | 403 | Insufficient permissions |

---

## Rate Limits

| API | Limit | Window |
|-----|-------|--------|
| Public API | 100 req | 1 minute |
| Admin API | 1000 req | 1 minute |
| API v1 | 60 req | 1 minute |

**Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1710080400
```

---

## Testing

### Test Cards (Stripe)
- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **3D Secure**: `4000 0027 6000 3184`

### Demo Mode
```bash
# Enable in application.properties
spring.profiles.active=dev,demo
```

---

## Useful Snippets

### JavaScript/Fetch
```javascript
// Get events
fetch('https://your-domain.com/api/v2/public/events')
  .then(res => res.json())
  .then(events => console.log(events));

// Create reservation
fetch('https://your-domain.com/api/v2/public/event/conf-2024/reserve-tickets', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    reservation: [{ticketCategoryId: 1, amount: 2}]
  })
})
  .then(res => res.json())
  .then(data => console.log(data.value)); // reservation ID
```

### Python/Requests
```python
import requests

# Get events
response = requests.get('https://your-domain.com/api/v2/public/events')
events = response.json()

# Create reservation
response = requests.post(
    'https://your-domain.com/api/v2/public/event/conf-2024/reserve-tickets',
    json={
        'reservation': [{'ticketCategoryId': 1, 'amount': 2}]
    }
)
reservation_id = response.json()['value']
```

### cURL with Auth
```bash
# Save credentials
API_USER="admin"
API_PASS="password"
API_BASE="https://your-domain.com"

# Use in requests
curl -u $API_USER:$API_PASS $API_BASE/admin/api/events
```

---

## Common Workflows

### 1. Complete Ticket Purchase
```bash
# Step 1: Get event details
EVENT_NAME="conference-2024"
curl https://domain.com/api/v2/public/event/$EVENT_NAME

# Step 2: Create reservation
RESERVATION=$(curl -X POST \
  https://domain.com/api/v2/public/event/$EVENT_NAME/reserve-tickets \
  -H "Content-Type: application/json" \
  -d '{"reservation": [{"ticketCategoryId": 1, "amount": 2}]}' \
  | jq -r '.value')

# Step 3: Fill booking data
curl -X POST \
  https://domain.com/api/v2/public/event/$EVENT_NAME/reservation/$RESERVATION/validate-to-overview \
  -H "Content-Type: application/json" \
  -d '{"tickets": {...}, "billingDetails": {...}}'

# Step 4: Complete payment
curl -X POST \
  https://domain.com/api/v2/public/event/$EVENT_NAME/reservation/$RESERVATION \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod": "STRIPE", "termAndConditionsAccepted": true, ...}'
```

### 2. Bulk Check-In
```bash
# Get all tickets for event
curl -u admin:pass \
  "https://domain.com/admin/api/reservation/event/conf-2024/reservations/all-status?status=COMPLETE" \
  | jq -r '.reservations[].tickets[].uuid' \
  > ticket_uuids.txt

# Check in each ticket
while read uuid; do
  curl -X POST -u admin:pass \
    https://domain.com/admin/api/check-in/event/conf-2024/ticket/scan \
    -H "Content-Type: application/json" \
    -d "{\"code\": \"$uuid\"}"
done < ticket_uuids.txt
```

### 3. Export Attendees
```bash
# Export to Excel
curl -u admin:pass \
  "https://domain.com/admin/api/events/conference-2024/export?format=excel" \
  -o attendees.xlsx

# Export to CSV
curl -u admin:pass \
  "https://domain.com/admin/api/events/conference-2024/export?format=csv" \
  -o attendees.csv
```

---

## Best Practices

### 1. Always Check Response
```bash
RESPONSE=$(curl -s -w "\n%{http_code}" https://domain.com/api/v2/public/events)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ $HTTP_CODE -eq 200 ]; then
  echo "Success: $BODY"
else
  echo "Error $HTTP_CODE: $BODY"
fi
```

### 2. Use Pagination
```bash
# Fetch all pages
PAGE=0
while true; do
  RESPONSE=$(curl -u admin:pass \
    "https://domain.com/admin/api/reservation/event/conf/reservations/all-status?page=$PAGE")
  
  TOTAL_PAGES=$(echo $RESPONSE | jq -r '.totalPages')
  
  # Process results
  echo $RESPONSE | jq '.reservations'
  
  PAGE=$((PAGE + 1))
  [ $PAGE -ge $TOTAL_PAGES ] && break
done
```

### 3. Handle Rate Limits
```bash
make_request() {
  RESPONSE=$(curl -s -w "\n%{http_code}" "$1")
  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
  
  if [ $HTTP_CODE -eq 429 ]; then
    echo "Rate limited, waiting..."
    sleep 60
    make_request "$1"  # Retry
  else
    echo "$RESPONSE" | sed '$d'
  fi
}

make_request "https://domain.com/api/v2/public/events"
```

---

**Quick Reference Version**: 1.0  
**Last Updated**: February 12, 2026

