# Inventory Subsystem Reference

This document provides a technical guide to the Inventory management module, detailing the backend model, repository actions, controller implementation, and safety validation boundaries.

## 1. Data Model Configuration

The inventory model is declared in `InventoryItem.java` with the following attributes:

*   `id` (`String`): Unique key generated automatically as `INV-XXX` (e.g., `INV-001`).
*   `name` (`String`): Readable label of the stock item.
*   `category` (`String`): Structural classification filter. Pre-set defaults include: `Water`, `Grain`, `Feed`, and `Others`.
*   `quantity` (`double`): Floating-point storage balance tracking.
*   `unit` (`String`): Physical tracking metric (e.g., `Liters`, `Kilograms`).

---

## 2. Business Flow & Database Actions

The interface uses standard SQLite CRUD queries via helper functions centralized inside `FarmRepository.java`:

*   **Fetching Inventory**: `FarmRepository.getAllInventory()` fetches the collection of stock.
*   **Updating Balances**: `FarmRepository.updateItemQuantity(InventoryItem item)` syncs local in-memory adjustments back to the SQLite physical records.
*   **Deletion**: `FarmRepository.removeStaticItem(InventoryItem item)` purges records from the storage system.

---

## 3. Operations & Triggers

### Stock Top-Up and Consumption
When a user updates inventory balances inside `InventoryController.java` (`handleAddStock()` or `handleConsumeStock()`):
1.  The quantity attribute is incremented or decremented on the active `InventoryItem` instance.
2.  The update is pushed to SQLite through `FarmRepository.updateItemQuantity(...)`.
3.  **Threshold Monitoring**: `NotificationService.getInstance().checkInventoryThresholds(selectedItem)` executes instantly to evaluate if quantity drops below critical boundaries (generating notification events where required).

---

## 4. Input Validation Standards

To prevent database insertion errors or visual anomalies in standard table panels, input fields enforce character limits:

| Parameter | Allowed Character Regex | Allowed Symbols |
|---|---|---|
| **Item Name** | `^[a-zA-Z0-9\s._-]+$` | Alphanumeric, spaces, periods (`.`), dashes (`-`), and underscores (`_`) |
| **Unit Type** | `^[a-zA-Z\s]+$` | Letters and spaces only |
| **Quantities** | Positive double values | Numerical formats only |
