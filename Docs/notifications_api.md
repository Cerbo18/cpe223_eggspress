# Static Notification API Reference

This document serves as a developer guide for triggering global alerts and localized user feedbacks through the centralized notification service.

## 1. Core Service Architecture

The system decouples notification triggers from active UI views using helper endpoints in `NotificationService.java`. When an API endpoint is called:
1.  The event is immediately persisted in the local database.
2.  An asynchronous notification listener updates active dashboard lists.
3.  A slide-in visual alert (push toast) is executed safely on the application's primary UI thread.

---

## 2. API Endpoint Reference

All methods are accessed statically. Do not instantiate database repository instances inside view controllers.

### A. General-Purpose Methods

#### `NotificationService.notificationInfo(String message)`
*   **Default Visibility**: Local (Targets the active user session only).
*   **Typical Context**: Input validator responses, layout modifications, successful local state updates.

#### `NotificationService.notificationWarning(String message)`
*   **Default Visibility**: Global (Visible to all users in the system).
*   **Typical Context**: Stale hardware connections, threshold margins exceeded.

#### `NotificationService.notificationCritical(String message)`
*   **Default Visibility**: Global (Visible to all users in the system).
*   **Typical Context**: Core database errors, environment failures, immediate automated safety shutdowns.

---

### B. Explicit Scope Overrides

To enforce a specific visibility pattern, use the double-parameter overloaded variations:

```java
// Force an informative event to publish globally to all users:
NotificationService.notificationInfo("Automated system-wide inventory audit complete.", true);

// Send a critical validation failure strictly to the active operator:
NotificationService.notificationCritical("Database transaction failed: Duplicate SKU profile.", false);
```
