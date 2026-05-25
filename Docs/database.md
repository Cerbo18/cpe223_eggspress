# Database Schema & Management

This document defines the local persistence architecture, entity relationships, visibility scopes, and lifecycle actions for new account registrations.

## 1. Database Infrastructure

Eggspress utilizes an embedded, single-file relational database managed via the SQLite driver. 
*   **Database File Location**: `database/eggspress.db`
*   **Connection Driver Control**: Handled centrally within `DatabaseConfig.java`. Connection pools, table creation statements, and schema changes are processed synchronously during application startup.

---

## 2. Notification Scoping and Visibility Schemas

To isolate user-private alerts from system-wide notifications, the storage schema employs a nullable key indicator:

```sql
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message TEXT NOT NULL,
    type TEXT NOT NULL,          -- 'INFO', 'WARNING', 'CRITICAL'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    username TEXT NULL           -- Scope Key: NULL = Global Alert, non-NULL = Local Target
);
```

### Visibility Rule Sets
1.  **Global Scope (`username IS NULL`)**:
    *   Saves with a `NULL` username key.
    *   Fetched by all logged-in users during database polling cycles.
2.  **Local Scope (`username = 'specific_user'`)**:
    *   Saves with the target user's identifier.
    *   Fetched exclusively by the matching active user. Other active sessions filter out these records at the query level.

---

## 3. Transaction-Safe Account Initialization

When registering a new user profile via `UserRepository.java`, pre-existing global notifications present in the database must not flood the new user's active interface.

To prevent this:
1.  Registration queries execute in a single, transaction-safe block.
2.  Upon successfully saving a new user record, an automated transaction inserts entries into the tracking table `user_notification_states` mapping all existing historical notification IDs for the new username as:
    *   `is_read = 1`
    *   `is_cleared = 1`
3.  This isolation routine ensures that new accounts begin their lifecycle with an empty inbox view, while dynamically preserving the history of legacy accounts.
