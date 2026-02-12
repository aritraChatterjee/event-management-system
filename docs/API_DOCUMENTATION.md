# API Documentation - Alf.io Event Management System

## Overview

The Alf.io REST API provides programmatic access to event management functionality. The API is organized around REST principles and returns JSON responses.

**Base URL**: `https://your-domain.com`  
**API Versions**: v1, v2  
**Authentication**: HTTP Basic Auth, OAuth2, API Keys  
**Format**: JSON

---

## Table of Contents

1. [Authentication](#authentication)
2. [Public API (v2)](#public-api-v2)
3. [Admin API](#admin-api)
4. [API v1 (Legacy)](#api-v1-legacy)
5. [Payment API](#payment-api)
6. [Common Patterns](#common-patterns)
7. [Error Handling](#error-handling)
8. [Rate Limiting](#rate-limiting)

---

## Authentication

### Public Endpoints

No authentication required for:
- Event listing
- Event details
- Ticket availability
- Public reservations

### Admin Endpoints

Require authentication via:

**HTTP Basic Auth**:
```http
Authorization: Basic base64(username:password)
```

**API Key** (for system integration):
```http
Authorization: ApiKey your-api-key-here
```

**Session Cookie** (for web interface):
```http
Cookie: JSESSIONID=...
```

---

## Public API (v2)

Base path: `/api/v2/public/`

### Events

#### List All Events

```http
GET /api/v2/public/events
```

**Response**:
```json
[
  {
    "id": 1,
    "shortName": "conference-2024",
    "displayName": "Tech Conference 2024",
    "location": "San Francisco, CA",
    "format": "IN_PERSON",
    "startDate": "2024-06-15T09:00:00Z",
    "endDate": "2024-06-17T18:00:00Z",
    "timezone": "America/Los_Angeles",
    "currency": "USD",
    "availableSeats": 500,
    "organizationName": "Tech Events Inc"
  }
]
```

#### Get Event Details

```http
GET /api/v2/public/event/{eventName}
```

**Parameters**:
- `eventName` (path) - Event short name

**Response**:
```json
{
  "event": {
    "id": 1,
    "shortName": "conference-2024",
    "displayName": "Tech Conference 2024",
    "websiteUrl": "https://conference.example.com",
    "location": "San Francisco Convention Center",
    "latitude": "37.7749",
    "longitude": "-122.4194",
    "startDate": "2024-06-15T09:00:00-07:00",
    "endDate": "2024-06-17T18:00:00-07:00",
    "timezone": "America/Los_Angeles",
    "format": "IN_PERSON",
    "currency": "USD",
    "vatIncluded": true,
    "vat": 8.5
  },
  "organization": {
    "name": "Tech Events Inc",
    "email": "info@techevents.com"
  },
  "ticketCategories": [
    {
      "id": 1,
      "name": "General Admission",
      "description": "Standard entry ticket",
      "price": 299.00,
      "maxTickets": 400,
      "availableTickets": 245,
      "inception": "2024-01-01T00:00:00Z",
      "expiration": "2024-06-14T23:59:59Z",
      "bounded": true,
      "accessRestricted": false
    }
  ],
  "additionalServices": [
    {
      "id": 1,
      "name": "Workshop Access",
      "description": "Access to premium workshops",
      "price": 150.00,
      "type": "SUPPLEMENT",
      "availableItems": 50
    }
  ]
}
```

#### Get Ticket Categories

```http
GET /api/v2/public/event/{eventName}/ticket-categories
```

**Query Parameters**:
- `code` (optional) - Promo code to reveal hidden categories

**Response**:
```json
{
  "ticketCategories": [
    {
      "id": 1,
      "name": "Early Bird",
      "price": 249.00,
      "available": true,
      "saleable": true,
      "formattedInception": "Jan 1, 2024",
      "formattedExpiration": "Mar 31, 2024"
    }
  ],
  "expiredCategories": [],
  "waitingList": false,
  "preSales": false
}
```

---

### Reservations

#### Create Reservation

```http
POST /api/v2/public/event/{eventName}/reserve-tickets
```

**Request Body**:
```json
{
  "reservation": [
    {
      "ticketCategoryId": 1,
      "amount": 2
    }
  ],
  "additionalService": [
    {
      "additionalServiceId": 1,
      "quantity": 2
    }
  ],
  "promoCode": "EARLYBIRD",
  "captcha": "captcha-token"
}
```

**Response**:
```json
{
  "success": true,
  "value": "reservation-uuid-here",
  "errors": []
}
```

#### Get Reservation Details

```http
GET /api/v2/public/event/{eventName}/reservation/{reservationId}
```

**Response**:
```json
{
  "id": "abc-123-def",
  "shortId": "ABC123",
  "status": "PENDING",
  "validUntil": "2024-03-15T14:30:00Z",
  "email": "customer@example.com",
  "fullName": "John Doe",
  "orderSummary": {
    "summary": [
      {
        "name": "General Admission x 2",
        "amount": 2,
        "price": "$299.00",
        "subTotal": "$598.00",
        "type": "TICKET"
      }
    ],
    "totalPrice": "$598.00",
    "priceInCents": 59800,
    "free": false
  },
  "tickets": [
    {
      "uuid": "ticket-uuid-1",
      "firstName": "",
      "lastName": "",
      "email": "",
      "assigned": false,
      "lockedAssignment": false
    }
  ]
}
```

#### Validate Booking Data

```http
POST /api/v2/public/event/{eventName}/reservation/{reservationId}/validate-to-overview
```

**Request Body**:
```json
{
  "tickets": {
    "ticket-uuid-1": {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    }
  },
  "billingDetails": {
    "customerReference": null,
    "skipVatNr": false,
    "invoiceRequested": true
  }
}
```

**Response**:
```json
{
  "success": true,
  "errorCount": 0
}
```

#### Confirm Reservation

```http
POST /api/v2/public/event/{eventName}/reservation/{reservationId}
```

**Request Body**:
```json
{
  "paymentMethod": "STRIPE",
  "termAndConditionsAccepted": true,
  "privacyPolicyAccepted": true,
  "paymentToken": "payment-token-from-stripe",
  "gatewayToken": null
}
```

**Response**:
```json
{
  "success": true,
  "redirect": "/event/conference-2024/reservation/abc-123-def/success",
  "reservation": {
    "id": "abc-123-def",
    "status": "COMPLETE"
  }
}
```

---

### Subscriptions

#### List Active Subscriptions

```http
GET /api/v2/public/subscriptions
```

**Query Parameters**:
- `organizationSlug` (optional) - Filter by organization

**Response**:
```json
[
  {
    "id": "subscription-uuid",
    "title": {
      "en": "Annual Pass"
    },
    "description": {
      "en": "Unlimited access to all events"
    },
    "price": 999.00,
    "currency": "USD",
    "maxAvailable": 100,
    "onSaleFrom": "2024-01-01T00:00:00Z",
    "onSaleTo": "2024-12-31T23:59:59Z"
  }
]
```

#### Get Subscription Details

```http
GET /api/v2/public/subscription/{subscriptionId}
```

**Response**:
```json
{
  "id": "subscription-uuid",
  "title": {
    "en": "Annual Pass"
  },
  "description": {
    "en": "Unlimited access to all events for one year"
  },
  "price": 999.00,
  "currency": "USD",
  "validityType": "STANDARD",
  "validityUnits": 12,
  "validityTimeUnit": "MONTHS",
  "usageType": "UNLIMITED",
  "maxEntries": null,
  "termsAndConditionsUrl": "https://example.com/terms",
  "privacyPolicyUrl": "https://example.com/privacy"
}
```

---

### Tickets

#### Get Ticket Details

```http
GET /api/v2/public/event/{eventName}/ticket/{ticketId}
```

**Query Parameters**:
- `ticketIdentifier` - Public ticket UUID (from QR code)

**Response**:
```json
{
  "uuid": "ticket-public-uuid",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "category": "General Admission",
  "assigned": true,
  "status": "ACQUIRED",
  "qrCode": "data:image/png;base64,..."
}
```

#### Update Ticket

```http
PUT /api/v2/public/event/{eventName}/ticket/{ticketId}
```

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "userLanguage": "en"
}
```

**Response**:
```json
{
  "success": true
}
```

---

### Polls

#### Get Active Polls

```http
GET /api/v2/public/event/{eventName}/poll
```

**Response**:
```json
[
  {
    "id": 1,
    "title": "Session Feedback",
    "description": "Rate the keynote session",
    "status": "OPEN",
    "options": [
      {
        "id": 1,
        "title": "Excellent"
      },
      {
        "id": 2,
        "title": "Good"
      }
    ]
  }
]
```

#### Vote in Poll

```http
POST /api/v2/public/event/{eventName}/poll/{pollId}/vote
```

**Request Body**:
```json
{
  "reservationId": "abc-123-def",
  "optionId": 1
}
```

**Response**:
```json
{
  "success": true
}
```

---

## Admin API

Base path: `/admin/api/`

**Authentication Required**: All endpoints require admin authentication.

### Events

#### List All Events

```http
GET /admin/api/events
```

**Headers**:
```http
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
```

**Response**:
```json
[
  {
    "id": 1,
    "displayName": "Tech Conference 2024",
    "shortName": "conference-2024",
    "organizationId": 1,
    "organizationName": "Tech Events Inc",
    "location": "San Francisco",
    "startDate": "2024-06-15T09:00:00Z",
    "endDate": "2024-06-17T18:00:00Z",
    "soldTickets": 255,
    "allocatedTickets": 500,
    "status": "PUBLIC"
  }
]
```

#### Get Event Details

```http
GET /admin/api/events/{eventName}
```

**Response**:
```json
{
  "event": {
    "id": 1,
    "shortName": "conference-2024",
    "displayName": "Tech Conference 2024",
    "websiteUrl": "https://conference.example.com",
    "location": "San Francisco Convention Center",
    "latitude": "37.7749",
    "longitude": "-122.4194",
    "startDate": "2024-06-15T09:00:00Z",
    "endDate": "2024-06-17T18:00:00Z",
    "regularPrice": 29900,
    "currency": "USD",
    "availableSeats": 500,
    "vatIncluded": true,
    "vat": 8.5,
    "allowedPaymentProxies": ["STRIPE", "PAYPAL", "OFFLINE"],
    "status": "PUBLIC"
  },
  "organization": {
    "id": 1,
    "name": "Tech Events Inc",
    "email": "info@techevents.com"
  }
}
```

#### Create Event

```http
POST /admin/api/events/new
```

**Request Body**:
```json
{
  "shortName": "conference-2024",
  "displayName": "Tech Conference 2024",
  "organizationId": 1,
  "location": "San Francisco Convention Center",
  "latitude": "37.7749",
  "longitude": "-122.4194",
  "startDate": {
    "date": "2024-06-15",
    "time": "09:00"
  },
  "endDate": {
    "date": "2024-06-17",
    "time": "18:00"
  },
  "timezone": "America/Los_Angeles",
  "websiteUrl": "https://conference.example.com",
  "termsAndConditionsUrl": "https://conference.example.com/terms",
  "privacyPolicyUrl": "https://conference.example.com/privacy",
  "imageUrl": null,
  "regularPrice": 299.00,
  "currency": "USD",
  "availableSeats": 500,
  "vatIncluded": true,
  "vat": 8.5,
  "allowedPaymentProxies": ["STRIPE", "PAYPAL"],
  "ticketCategories": [
    {
      "name": "General Admission",
      "description": "Standard entry",
      "maxTickets": 400,
      "price": 299.00,
      "bounded": true,
      "accessRestricted": false,
      "inception": {
        "date": "2024-01-01",
        "time": "00:00"
      },
      "expiration": {
        "date": "2024-06-14",
        "time": "23:59"
      }
    }
  ]
}
```

**Response**:
```json
{
  "success": true,
  "errorCount": 0,
  "value": 1
}
```

#### Update Event

```http
POST /admin/api/events/{eventId}/header/update
```

**Request Body**:
```json
{
  "displayName": "Tech Conference 2024 - Updated",
  "websiteUrl": "https://conference.example.com",
  "location": "San Francisco",
  "description": {
    "en": "Updated description"
  }
}
```

#### Delete Event

```http
DELETE /admin/api/events/{eventId}
```

**Response**: `200 OK`

---

### Ticket Categories

#### List Categories

```http
GET /admin/api/event/{eventName}/categories
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "General Admission",
    "maxTickets": 400,
    "price": 29900,
    "bounded": true,
    "accessRestricted": false,
    "inception": "2024-01-01T00:00:00Z",
    "expiration": "2024-06-14T23:59:59Z",
    "soldTickets": 155,
    "pendingTickets": 10,
    "availableTickets": 235
  }
]
```

#### Create Category

```http
POST /admin/api/events/{eventId}/categories/new
```

**Request Body**:
```json
{
  "name": "VIP Pass",
  "description": "VIP access with perks",
  "maxTickets": 50,
  "price": 599.00,
  "bounded": true,
  "accessRestricted": true,
  "inception": {
    "date": "2024-01-01",
    "time": "00:00"
  },
  "expiration": {
    "date": "2024-06-14",
    "time": "23:59"
  }
}
```

#### Update Category

```http
POST /admin/api/events/{eventId}/categories/{categoryId}/update
```

---

### Reservations

#### List Reservations

```http
GET /admin/api/reservation/{purchaseContextType}/{publicIdentifier}/reservations/all-status
```

**Parameters**:
- `purchaseContextType` - "event" or "subscription"
- `publicIdentifier` - Event short name or subscription ID

**Query Parameters**:
- `page` - Page number (default: 0)
- `search` - Search term
- `orderBy` - Sort field
- `direction` - "ASC" or "DESC"
- `status` - Filter by status

**Response**:
```json
{
  "page": 0,
  "pageSize": 50,
  "totalPages": 10,
  "totalElements": 487,
  "reservations": [
    {
      "id": "abc-123-def",
      "shortId": "ABC123",
      "fullName": "John Doe",
      "email": "john@example.com",
      "confirmationTimestamp": "2024-03-10T15:30:00Z",
      "status": "COMPLETE",
      "ticketsCount": 2,
      "formattedAmount": "$598.00",
      "currencyCode": "USD"
    }
  ]
}
```

#### Get Reservation Details

```http
GET /admin/api/reservation/{purchaseContextType}/{publicIdentifier}/{reservationId}
```

**Response**:
```json
{
  "id": "abc-123-def",
  "shortId": "ABC123",
  "status": "COMPLETE",
  "customerData": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "billingAddress": {
      "company": "ACME Corp",
      "line1": "123 Main St",
      "city": "San Francisco",
      "zip": "94102",
      "country": "US",
      "taxId": "12-3456789"
    }
  },
  "tickets": [
    {
      "uuid": "ticket-uuid-1",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "category": "General Admission",
      "status": "ACQUIRED",
      "checkedIn": false
    }
  ],
  "invoices": [
    {
      "id": 1,
      "number": "INV-2024-001",
      "type": "INVOICE",
      "generationTimestamp": "2024-03-10T15:30:00Z"
    }
  ]
}
```

#### Create Manual Reservation

```http
POST /admin/api/reservation/{purchaseContextType}/{publicIdentifier}/new
```

**Request Body**:
```json
{
  "tickets": [
    {
      "categoryId": 1,
      "attendees": [
        {
          "firstName": "John",
          "lastName": "Doe",
          "emailAddress": "john@example.com"
        },
        {
          "firstName": "Jane",
          "lastName": "Doe",
          "emailAddress": "jane@example.com"
        }
      ]
    }
  ],
  "additionalServices": [
    {
      "additionalServiceId": 1,
      "quantity": 2
    }
  ],
  "customerData": {
    "firstName": "John",
    "lastName": "Doe",
    "emailAddress": "john@example.com"
  },
  "notification": {
    "customer": true,
    "attendees": false
  }
}
```

**Response**:
```json
{
  "success": true,
  "value": "new-reservation-id"
}
```

---

### Check-In

#### Get Event Check-In Status

```http
GET /admin/api/check-in/{eventName}
```

**Response**:
```json
{
  "statistics": {
    "totalTickets": 500,
    "checkedIn": 245,
    "notCheckedIn": 255
  },
  "categoryStatistics": [
    {
      "categoryName": "General Admission",
      "totalTickets": 400,
      "checkedIn": 200
    }
  ]
}
```

#### Check In Ticket

```http
POST /admin/api/check-in/event/{eventName}/ticket/{ticketIdentifier}
```

**Request Body**:
```json
{
  "code": "ticket-public-uuid",
  "scan_timestamp": 1710080400000,
  "force": false
}
```

**Response**:
```json
{
  "result": {
    "status": "SUCCESS",
    "message": "Ticket checked in successfully",
    "ticket": {
      "uuid": "ticket-uuid",
      "firstName": "John",
      "lastName": "Doe",
      "category": "General Admission",
      "dups": 0
    }
  }
}
```

#### Manual Check-In

```http
POST /admin/api/check-in/event/{eventName}/ticket/{ticketIdentifier}/manual-check-in
```

---

### Promo Codes

#### List Promo Codes

```http
GET /admin/api/events/{eventId}/promo-code
```

**Response**:
```json
[
  {
    "id": 1,
    "promoCode": "EARLYBIRD",
    "eventId": 1,
    "discountType": "PERCENTAGE",
    "discountAmount": 20,
    "validFrom": "2024-01-01T00:00:00Z",
    "validTo": "2024-03-31T23:59:59Z",
    "maxUsage": 100,
    "currentUsage": 45,
    "categories": [1, 2]
  }
]
```

#### Create Promo Code

```http
POST /admin/api/events/{eventId}/promo-code
```

**Request Body**:
```json
{
  "promoCode": "SUMMER25",
  "discountType": "PERCENTAGE",
  "discountAmount": 25,
  "validFrom": {
    "date": "2024-06-01",
    "time": "00:00"
  },
  "validTo": {
    "date": "2024-08-31",
    "time": "23:59"
  },
  "maxUsage": 200,
  "categories": [1],
  "codeType": "DISCOUNT",
  "description": "Summer discount"
}
```

#### Delete Promo Code

```http
DELETE /admin/api/events/{eventId}/promo-code/{promoCodeId}
```

---

### Additional Services

#### List Additional Services

```http
GET /admin/api/event/{eventName}/additional-services
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "Workshop Access",
    "type": "SUPPLEMENT",
    "price": 15000,
    "fixPrice": true,
    "availableQuantity": 50,
    "maxQtyPerOrder": 4,
    "supplementPolicy": "OPTIONAL_MAX_AMOUNT_PER_TICKET"
  }
]
```

#### Create Additional Service

```http
POST /admin/api/event/{eventName}/additional-services/new
```

**Request Body**:
```json
{
  "name": "Parking Pass",
  "description": "Reserved parking spot",
  "type": "SUPPLEMENT",
  "price": 25.00,
  "fixPrice": true,
  "availableQuantity": 100,
  "maxQtyPerOrder": 2,
  "supplementPolicy": "OPTIONAL_MAX_AMOUNT_PER_RESERVATION",
  "inception": {
    "date": "2024-01-01",
    "time": "00:00"
  },
  "expiration": {
    "date": "2024-06-15",
    "time": "09:00"
  }
}
```

---

### Users & Organizations

#### List Users

```http
GET /admin/api/users
```

**Response**:
```json
[
  {
    "id": 1,
    "username": "admin",
    "firstName": "John",
    "lastName": "Admin",
    "emailAddress": "admin@example.com",
    "type": "INTERNAL",
    "enabled": true,
    "role": "ROLE_ADMIN"
  }
]
```

#### Create User

```http
POST /admin/api/users/new
```

**Request Body**:
```json
{
  "username": "newuser",
  "firstName": "Jane",
  "lastName": "User",
  "emailAddress": "jane@example.com",
  "organizationId": 1,
  "role": "ROLE_OPERATOR",
  "description": "Event operator"
}
```

#### List Organizations

```http
GET /admin/api/organizations
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "Tech Events Inc",
    "email": "info@techevents.com",
    "description": "Technology event organizer",
    "slug": "tech-events-inc"
  }
]
```

---

### Subscriptions (Admin)

#### List Subscriptions

```http
GET /admin/api/organization/{organizationId}/subscription/list
```

**Response**:
```json
[
  {
    "id": "subscription-uuid",
    "title": "Annual Pass",
    "maxAvailable": 100,
    "onSaleFrom": "2024-01-01T00:00:00Z",
    "onSaleTo": "2024-12-31T23:59:59Z",
    "price": 99900,
    "currency": "USD",
    "status": "ACTIVE",
    "soldCount": 45,
    "allocatedCount": 50
  }
]
```

#### Create Subscription

```http
POST /admin/api/organization/{organizationId}/subscription
```

**Request Body**:
```json
{
  "title": {
    "en": "Premium Annual Pass"
  },
  "description": {
    "en": "Unlimited event access for one year"
  },
  "maxAvailable": 500,
  "onSaleFrom": {
    "date": "2024-01-01",
    "time": "00:00"
  },
  "onSaleTo": {
    "date": "2024-12-31",
    "time": "23:59"
  },
  "price": 999.00,
  "currency": "USD",
  "validityType": "STANDARD",
  "validityUnits": 12,
  "validityTimeUnit": "MONTHS",
  "usageType": "UNLIMITED",
  "paymentProxies": ["STRIPE", "PAYPAL"]
}
```

---

### Polls (Admin)

#### List Event Polls

```http
GET /admin/api/{eventName}/poll
```

**Response**:
```json
[
  {
    "id": 1,
    "title": "Session Feedback",
    "description": "Rate the sessions",
    "status": "OPEN",
    "order": 1,
    "options": [
      {
        "id": 1,
        "title": {"en": "Excellent"},
        "voteCount": 45
      }
    ]
  }
]
```

#### Create Poll

```http
POST /admin/api/{eventName}/poll
```

**Request Body**:
```json
{
  "title": "Lunch Preference",
  "description": "Choose your lunch option",
  "status": "OPEN",
  "order": 1,
  "options": [
    {
      "title": {"en": "Vegetarian"}
    },
    {
      "title": {"en": "Non-Vegetarian"}
    },
    {
      "title": {"en": "Vegan"}
    }
  ]
}
```

---

### Export & Reports

#### Export Attendees

```http
GET /admin/api/events/{eventName}/export
```

**Query Parameters**:
- `format` - "excel" or "csv"

**Response**: Excel/CSV file download

#### Export Reservations

```http
GET /admin/api/reservation/event/{eventName}/export
```

**Query Parameters**:
- `format` - "excel" or "csv"
- `status` - Filter by status

---

### Configuration

#### Get Event Configuration

```http
GET /admin/api/configuration/events/{eventName}
```

**Response**:
```json
{
  "configurations": [
    {
      "key": "GOOGLE_ANALYTICS_KEY",
      "value": "UA-XXXXXXX-X",
      "configurationLevel": "EVENT",
      "description": "Google Analytics tracking ID"
    }
  ]
}
```

#### Update Configuration

```http
POST /admin/api/configuration/events/{eventName}/update
```

**Request Body**:
```json
{
  "key": "STRIPE_PUBLIC_KEY",
  "value": "pk_live_..."
}
```

---

## API v1 (Legacy)

Base path: `/api/v1/admin/`

**Note**: V1 API is maintained for backward compatibility. New integrations should use V2 or Admin API.

### Create Reservation (V1)

```http
POST /api/v1/admin/event/{slug}/reservation
```

**Headers**:
```http
Authorization: ApiKey your-api-key
```

**Request Body**:
```json
{
  "tickets": [
    {
      "categoryId": 1,
      "attendees": [
        {
          "firstName": "John",
          "lastName": "Doe",
          "emailAddress": "john@example.com"
        }
      ]
    }
  ],
  "contactData": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  },
  "language": "en"
}
```

**Response**:
```json
{
  "id": "reservation-id",
  "status": "COMPLETE",
  "confirmationEmailSent": true
}
```

---

## Payment API

### Process Payment (Stripe)

```http
POST /api/stripe/payment/{eventName}/reservation/{reservationId}
```

**Request Body**:
```json
{
  "paymentMethodId": "pm_card_...",
  "termAndConditionsAccepted": true
}
```

**Response**:
```json
{
  "requiresAction": false,
  "paymentIntentClientSecret": null,
  "redirect": true
}
```

### Process Payment (PayPal)

```http
POST /api/paypal/create-order/{eventName}/reservation/{reservationId}
```

**Response**:
```json
{
  "orderId": "paypal-order-id",
  "approveUrl": "https://paypal.com/checkoutnow?token=..."
}
```

---

## Common Patterns

### Pagination

Endpoints that return lists support pagination:

```http
GET /admin/api/reservation/event/{eventName}/reservations/all-status?page=0&pageSize=50
```

**Response**:
```json
{
  "page": 0,
  "pageSize": 50,
  "totalPages": 10,
  "totalElements": 487,
  "results": [...]
}
```

### Filtering

Use query parameters:

```http
GET /admin/api/events?status=PUBLIC&organizationId=1
```

### Sorting

```http
GET /admin/api/reservations?orderBy=confirmationTimestamp&direction=DESC
```

### Search

```http
GET /admin/api/reservations?search=john@example.com
```

---

## Error Handling

### Error Response Format

```json
{
  "success": false,
  "errorCount": 1,
  "errors": [
    {
      "field": "email",
      "message": "Email address is required",
      "code": "error.email.required"
    }
  ]
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created |
| 204 | No Content | Request succeeded, no content to return |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 422 | Unprocessable Entity | Validation failed |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

### Common Error Codes

| Code | Description |
|------|-------------|
| `error.email.required` | Email address is required |
| `error.category.not.found` | Ticket category not found |
| `error.notEnoughTickets` | Not enough tickets available |
| `error.promo.code.invalid` | Invalid promo code |
| `error.reservation.expired` | Reservation has expired |
| `error.payment.failed` | Payment processing failed |

---

## Rate Limiting

### Limits

- **Public API**: 100 requests per minute per IP
- **Admin API**: 1000 requests per minute per user
- **API v1**: 60 requests per minute per API key

### Rate Limit Headers

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1710080400
```

### Exceeding Rate Limit

Response:
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60

{
  "error": "Rate limit exceeded. Please try again in 60 seconds."
}
```

---

## Webhooks

Alf.io can send webhooks for certain events using the Extension system.

### Supported Events

- `RESERVATION_CONFIRMED` - Reservation completed
- `TICKET_ASSIGNED` - Ticket assigned to attendee
- `TICKET_CHECKED_IN` - Ticket checked in
- `RESERVATION_CANCELLED` - Reservation cancelled

### Webhook Payload

```json
{
  "event": "RESERVATION_CONFIRMED",
  "timestamp": "2024-03-10T15:30:00Z",
  "eventShortName": "conference-2024",
  "reservationId": "abc-123-def",
  "data": {
    "fullName": "John Doe",
    "email": "john@example.com",
    "ticketCount": 2,
    "totalAmount": 598.00
  }
}
```

---

## SDK & Libraries

### Official Libraries

- **JavaScript/TypeScript**: Coming soon
- **Python**: Coming soon
- **PHP**: Coming soon

### Community Libraries

Check the [Alf.io GitHub](https://github.com/alfio-event/alf.io) for community-contributed SDKs.

---

## Testing

### Sandbox Environment

Use test payment credentials:

**Stripe Test Cards**:
- Success: `4242 4242 4242 4242`
- Decline: `4000 0000 0000 0002`

**PayPal Sandbox**:
- Use PayPal sandbox account

### Demo Mode

Enable demo mode in configuration to test without actual payments.

---

## Support

### Documentation
- **Full Docs**: [docs/](../docs/)
- **Database Schema**: [docs/DATABASE_SCHEMA.md](../docs/DATABASE_SCHEMA.md)
- **DDD Mapping**: [docs/DDD_MAPPING.md](../docs/DDD_MAPPING.md)

### Community
- **GitHub**: https://github.com/alfio-event/alf.io
- **Issues**: https://github.com/alfio-event/alf.io/issues
- **Discussions**: https://github.com/alfio-event/alf.io/discussions

---

**API Documentation Version**: 1.0  
**Last Updated**: February 12, 2026  
**Alf.io Version**: 2.0-M6+

