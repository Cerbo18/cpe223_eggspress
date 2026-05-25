# Automation & Scheduling Subsystem Reference

This document provides a technical guide to the automated systems, water dispatch routines, and feeding schedule workflows.

## 1. Domain Entities

Automations operate using two core domain entities:

### A. FeedingSchedule
*   **Attributes**: `category` (`String`), `time` (`String`), `feedingType` (`String`), `status` (`String`).
*   **Purpose**: Records scheduled events for regular feed and hydration runs.

### B. Automation
*   **Attributes**: `id` (`String`), `name` (`String`), `location` (`String`), `amount` (`double`), `status` (`String`).
*   **Purpose**: Logs transactional histories of automated mechanical events (e.g. dispatched water flows).

---

## 2. Dispatch Control (Water Flow Routine)

`AutomationController.java` coordinates hydration distribution through `handleSendWater()`:
1.  **Source Check**: The operator selects a water source from inventory.
2.  **Stock Verification**: The system verifies the requested amount is available.
3.  **Deduction & Threshold Update**: Quantity is deducted from the source, persisted to the database via `FarmRepository.updateInventoryQuantity(...)`, and threshold triggers are checked.
4.  **Log Generation**: A new `Automation` record is saved in SQLite database logs through `FarmRepository.addAutomationLog(log)`.

---

## 3. Automated Database Seeding

To provide a consistent execution state at boot:
*   On screen loading, `AutomationController.java` evaluates database sizes.
*   If the local database contains only one manual record or is empty, the seeding routine (`seedSimulatedSchedules()`) automatically inserts a standard schedule map (covering water refills and high-protein feed mixtures) to ensure proper UI table population.

---

## 4. Input Validation Standards

To maintain clean data tables, schedule creations validate inputs prior to database commitment:

| Target Parameter | Verification Regular Expression | Prohibited Values |
|---|---|---|
| **Scheduled Time** | `^[a-zA-Z0-9\s:]+$` | Special symbols except colons (`:`) |
| **Feeding Type** | `^[a-zA-Z0-9\s._-]+$` | Special symbols except `.`, `-`, `_` |
| **Schedule Status** | `^[a-zA-Z\s]+$` | Numeric characters and punctuation marks |
