# Yamo Laundry Apps Implementation Guide

Audience: AI assistant(s) building the Flutter Customer app, Driver (Livreur) app, and Admin/Operator app.

This document defines the flows, endpoints, payloads, authentication, and streaming required to implement the three apps end-to-end.

Base URL examples
- Local: http://localhost:8080
- Production: https://api.yamo.example

General
- All requests and responses use JSON unless otherwise specified.
- Time fields are ISO8601 strings (e.g., 2025-09-17T15:30:00Z or local offset if applicable).
- Currency is XAF.
- Pagination is not yet implemented; list endpoints return full sets (optimize later if needed).

Authentication and roles
- JWT Bearer authentication will be used. Acquire token via POST /api/auth/login and include in headers:
  - Authorization: Bearer {token}
- Roles: CUSTOMER, LIVREUR, ADMIN.
- Public endpoints (no token required):
  - Swagger UI: /swagger-ui.html, /swagger-ui/**
  - OpenAPI spec: /v3/api-docs, /v3/api-docs/**
  - WhatsApp webhook: GET/POST /api/notifications/whatsapp/webhook
  - Geo: /api/geo/coverage, /api/geo/estimate (may be restricted in production)
- Protected endpoints require the Bearer token; server will reject with 401/403 otherwise.

Error handling
- On input or process errors, server returns HTTP 4xx or 5xx with a JSON body:
  - { "error": true, "message": "..." } or standard Spring validation error formats.
- Client should show user-friendly messages and allow retries.

Google Maps integration
- The client apps handle map rendering, Places autocomplete, and device location.
- Backend provides coverage and estimate endpoints using Google Distance Matrix.

WhatsApp notifications
- The backend sends template or text messages for transactional events:
  - Order created
  - Pickup scheduled
  - Delivery scheduled
  - Status changes (e.g., OUT_FOR_DELIVERY)
  - Invoice issued and link to PDF
- Inbound webhook captures replies. The client apps do not send WhatsApp themselves; they display status and links.

OpenAPI reference
- Runtime docs: /swagger-ui.html
- Machine-readable spec: /v3/api-docs
- Static snapshot: /static/openapi.yaml (served from /openapi.yaml if static hosting is enabled)

========================================
Customer App
========================================

Primary flows
1) Sign in
- POST /api/auth/login
  - Request: { username, password }
  - Response: { token, user: { id, role, customerId? } }
- Store token securely (e.g., flutter_secure_storage). Include Authorization header on subsequent calls.

2) Manage profile
- GET /api/customers/{id}
- Optional edit/delete (DELETE /api/customers/{id}).

3) Manage addresses
- GET /api/customers/{customerId}/addresses
- POST /api/customers/{customerId}/addresses
  - Request example:
    {
      "name": "Home",
      "district": "AKWA",  // enum; if not known, send street-only
      "street": "123 Main",
      "buildingNumber": "B2",
      "apartmentNumber": "12A",
      "additionalInstructions": "Near pharmacy",
      "latitude": 4.05,
      "longitude": 9.76,
      "primary": true
    }
- DELETE /api/addresses/{id}

4) Create order
- POST /api/orders
  - Request:
    {
      "customerId": <long>,
      "promoCode": "OPTIONAL",
      "items": [
        { "itemType": "SHIRT", "quantity": 5, "pricePerUnit": 500 },
        { "itemType": "PANT", "quantity": 2, "pricePerUnit": 800, "specialInstructions": "Delicate" }
      ]
    }
  - Response includes order id, items, totals, status.

5) Schedule pickup and delivery
- POST /api/orders/{id}/pickup
  - Request:
    { "contactName": "John", "contactPhone": "+2376...", "address": "Street, District, Douala", "scheduledDate": "2025-09-17T10:00:00" }
- POST /api/orders/{id}/delivery
  - Request:
    { "contactName": "John", "contactPhone": "+2376...", "address": "Street, District, Douala", "scheduledDate": "2025-09-20T10:00:00" }
- The backend auto-schedules delivery by default when pickup is scheduled; clients may reschedule via the delivery endpoint.

6) Monitor order status and tracking
- GET /api/orders?customerId={id}  // list
- GET /api/orders/{orderId}        // details
- POST /api/orders/{orderId}/status { "status": "..." }  // used mostly by admin/operator, but customers can cancel if allowed later
- Live tracking:
  - GET /api/orders/{orderId}/livreur/latest
  - GET /api/orders/{orderId}/livreur/stream  (SSE)
    - Use EventSource on Flutter via an SSE client plugin or manual HTTP stream handling.
    - On event named "location", payload structure:
      {
        "orderId": <long>,
        "livreurId": <long>,
        "lat": <double>,
        "lng": <double>,
        "heading": <double|null>,
        "speed": <double|null>,
        "ts": "2025-09-17T10:00:00"
      }
    - Reconnect with exponential backoff on disconnect.

7) Billing
- GET /api/billing/customers/{customerId}/invoices
- GET /api/billing/customers/{customerId}/payments
- GET /api/billing/customers/{customerId}/balance
- Download invoice PDF: GET /api/billing/invoices/pdf/{invoiceNumber}

8) Geo services
- POST /api/geo/coverage { lat, lng }
- POST /api/geo/estimate { originLat, originLng, destLat, destLng }

========================================
Driver (Livreur) App
========================================

Primary flows
1) Sign in
- POST /api/auth/login  -> token with role LIVREUR

2) Task list and details
- GET /api/tasks/livreur/{livreurId}?from=2025-09-17T00:00:00&to=2025-09-18T00:00:00
- GET /api/tasks?from=...&to=...  (if ADMIN, operator mode)

3) Update task status
- POST /api/tasks/{taskId}/status?status=IN_PROGRESS
- POST /api/tasks/{taskId}/status?status=DONE&notes=Delivered

4) Share location while on active job
- POST /api/livreurs/{id}/location
  - Request:
    { "lat": 4.05, "lng": 9.76, "heading": 270, "speed": 4.2, "orderId": 123 }
  - Send every 5–10 seconds during active pickup/delivery; pause otherwise.
  - Use a foreground service on Android; respect battery and privacy settings.
- Optional for debugging:
  - GET /api/livreurs/{id}/location/recent?minutes=30

========================================
Admin/Operator App
========================================

1) Sign in
- POST /api/auth/login  -> token with role ADMIN

2) Customers
- GET /api/customers
- POST /api/customers
- GET /api/customers/{id}
- DELETE /api/customers/{id}

3) Livreurs
- GET /api/livreurs?active=true
- POST /api/livreurs
- GET /api/livreurs/{id}
- PUT /api/livreurs/{id}
- DELETE /api/livreurs/{id}

4) Tasks
- POST /api/tasks  (optionally with ?customerId=&orderId=)
- POST /api/tasks/{taskId}/assign-livreur (body Livreur)
- POST /api/tasks/{taskId}/status?status=IN_PROGRESS|DONE|CANCELLED|MISSED
- GET /api/tasks?from=&to=
- GET /api/tasks/customer/{customerId}

5) Orders
- POST /api/orders
- GET /api/orders?customerId=
- GET /api/orders/{id}
- POST /api/orders/{id}/pickup
- POST /api/orders/{id}/delivery
- POST /api/orders/{id}/status

6) Billing and receivables
- POST /api/billing/orders/{orderId}/invoice?dueDate=YYYY-MM-DD
- POST /api/billing/payments?customerId=&amount=&method=&reference=&invoiceNumber=
- GET /api/billing/customers/{customerId}/invoices
- GET /api/billing/customers/{customerId}/payments
- GET /api/billing/customers/{customerId}/balance
- GET /api/billing/receivables?customerId=&from=&to=
- GET /api/billing/invoices/pdf/{invoiceNumber}
- POST /api/billing/invoices/{invoiceNumber}/send-whatsapp

7) Notifications
- POST /api/notifications/whatsapp/test { "to": "+2376...", "text": "..." }
- Webhook (configured at provider):
  - GET /api/notifications/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=...&hub.challenge=...
  - POST /api/notifications/whatsapp/webhook (provider calls this)

8) Geo utilities
- POST /api/geo/coverage { lat, lng }
- POST /api/geo/estimate { originLat, originLng, destLat, destLng }

========================================
Request/Response: Common Schemas
========================================
- Refer to the static OpenAPI file for complete schemas and types:
  - /openapi.yaml (if served from static) or project file src/main/resources/static/openapi.yaml
  - Runtime: /v3/api-docs

Key types
- Customer, Address, Order, OrderItem, Pickup, Delivery, Task, Livreur, LivreurLocation, Invoice, Payment.
- DTOs: OrderRequest, OrderItemRequest, PickupRequest, DeliveryRequest, StatusUpdateRequest, LocationUpdate.

========================================
SSE Streaming Guidance (Customer app)
========================================
- Endpoint: GET /api/orders/{orderId}/livreur/stream
- Headers: Authorization: Bearer {token}
- Client should:
  - Connect via SSE client library.
  - Handle event types:
    - name: init  -> confirms subscription
    - name: location -> contains latest GPS payload for map update
  - Reconnect on network errors with exponential backoff (e.g., 1s, 2s, 4s, up to 30s).
  - On reconnect, request latest via GET /api/orders/{orderId}/livreur/latest to quickly recover state.

========================================
Security and Configuration Expectations
========================================
- JWT: token provided via /api/auth/login. Include token on all protected calls.
- Roles:
  - CUSTOMER: Customer app
  - LIVREUR: Driver app
  - ADMIN: Admin/Operator app
- Backend environment variables:
  - WHATSAPP_PHONE_NUMBER_ID, WHATSAPP_TOKEN, WHATSAPP_VERIFY_TOKEN
  - GOOGLE_MAPS_API_KEY
  - PRICING_BASE_FEE, PRICING_PER_KM (optional overrides)
  - JWT_SECRET (to be set when JWT is fully wired)
- CORS: Allowed for all origins in development; restrict in production.

========================================
Client Implementation Checklist
========================================
Customer app
- [ ] Login flow and secure token storage
- [ ] Addresses CRUD (list/add/delete)
- [ ] Order creation with items and promo code
- [ ] Pickup and delivery scheduling
- [ ] Orders list/details with status updates
- [ ] Live tracking (SSE + latest endpoint)
- [ ] Billing: invoices, payments, balance, invoice PDF viewer
- [ ] Error handling and retries

Driver app
- [ ] Login as LIVREUR
- [ ] Task list (date range), status updates
- [ ] Foreground location service posting to backend periodically
- [ ] Efficient network usage, exponential backoff, and privacy controls

Admin/Operator app
- [ ] Login as ADMIN
- [ ] Customers CRUD
- [ ] Livreurs CRUD and filters
- [ ] Tasks management (create/assign/status/list)
- [ ] Orders management
- [ ] Billing issuance, payments, receivables
- [ ] WhatsApp test and webhook monitoring
- [ ] Geo coverage/estimate tools

========================================
Notes and Assumptions
========================================
- Pricing: backend computes linear fee via Distance Matrix result; frontends display estimates only.
- WhatsApp: use approved templates in production; free-form messages may be limited by provider rules.
- Internationalization: server messages are currently in French in several places; unify as needed.
- Future improvements: pagination, rate limiting, more granular permissions, push notifications, and background sync.
