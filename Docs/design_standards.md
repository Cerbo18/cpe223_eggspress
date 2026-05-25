# Design System & Styling Standards

This document establishes the official visual design system, color tokens, layout geometries, depth elevations, and separation of concerns rules.

## 1. Color Specification System

The user interface uses isolated light and dark themes managed strictly via custom CSS properties.

### A. Primary Brand Elements
*   `--brand-primary`: `#39b54a` (Vibrant Green)
*   `--brand-primary-hover`: `#319e3f` (Medium Green)
*   `--brand-primary-active`: `#2a8836` (Deep Green)

### B. Status Indicator Elements
*   **Critical Severity Level**: `#ff3b30` (Vibrant Red)
*   **Warning Severity Level**: `#ff9500` (Vibrant Amber/Orange)
*   **Info / Normal Level**: `#39b54a` (Brand Green)
*   **Inactive State**: `#86868b` (Neutral Grey)

### C. Color Palettes by Theme Mode

| Property Token | Light Theme Value | Dark Theme Value |
|---|---|---|
| Background Surface | `#f5f5f7` (Soft Light Grey) | `#161617` (Deep Charcoal Black) |
| Card Base Layer | `#ffffff` (Pure White) | `#2d2d2f` (Elevated Card Grey) |
| Core Typography | `#1d1d1f` (Deep Charcoal Black) | `#f5f5f7` (Soft Light Grey) |
| Secondary Typography | `#86868b` (Neutral Grey) | `#86868b` (Neutral Grey) |
| Input Fields Border | `#d2d2d7` (Soft Border Grey) | `#424245` (Medium Border Charcoal) |

---

## 2. Spatial Hierarchy & Geometry

Standardized paddings and corner configurations maintain consistent screen layouts.

*   **Corner Radii Properties**:
    *   `8px`: Form input boxes, combo-boxes, standard button elements.
    *   `12px`: Content container cards and floating dialog flyouts.
*   **Layout Spacers**:
    *   `20px`: Grid card internal layout padding.
    *   `10px 20px`: Button margins.
*   **Height Constraints**:
    *   `32px`: Persistent application navigation bar.
    *   `40px`: TableView grid row height constraints.

---

## 3. Depth & Elevation Standards

Overlays are visually separated from base layers using structured drop-shadow configurations.

*   **Level A Elevation (Standard Cards, Dismissible Status Cards)**:
    *   *Light Mode Shadow*: `dropshadow(gaussian, rgba(0, 0, 0, 0.14), 8, 0, 0, 2)`
    *   *Dark Mode Shadow*: `dropshadow(gaussian, rgba(0, 0, 0, 0.28), 8, 0, 0, 2)`
    *   *Radii*: `8px` corner boundary radius.
*   **Level B Elevation (Context Menus, Main Notification Flyout Panel)**:
    *   *Light Mode Shadow*: `dropshadow(gaussian, rgba(0, 0, 0, 0.14), 16, 0, 0, 4)`
    *   *Dark Mode Shadow*: `dropshadow(gaussian, rgba(0, 0, 0, 0.28), 16, 0, 0, 4)`
    *   *Radii*: `8px` corner boundary radius.

---

## 4. Separation of Concerns (SoC) Rule

> [!IMPORTANT]
> **No Programmatic Inline Styling**: Inline stylesheet manipulation using `.setStyle(...)` inside controller classes is strictly forbidden.
> Visual variables, hover colors, transitions, and dimension behaviors must live in CSS stylesheet resources.
> Toggle visibility states or highlight categories strictly by modifying style class names:
> ```java
> // Correct Interface Modification
> targetComponent.getStyleClass().add("status-critical");
> 
> // Incorrect Interface Modification
> targetComponent.setStyle("-fx-background-color: #ff3b30;");
> ```
