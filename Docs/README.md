# Eggspress Developer Documentation

Welcome to the official developer documentation for the Eggspress Chicken Farm Manager. This directory contains detailed reference manuals for various components of the codebase.

## Documentation Index

Please refer to the respective files below for deep technical details:

1.  **[Architecture & Directory Structures](architecture.md)**
    *   Structural overview of the application
    *   Model-View-Controller (MVC) layers and flow
    *   Directory mapping and responsibility boundaries

2.  **[View Routing & Window Navigation](view_routing.md)**
    *   JavaFX bootstrapping process and shell window layouts
    *   Collapsible sidebar mechanics and dynamic FXML loader wrapper configurations
    *   Detached popup styling layers, manual resizing cursor handlers, and thread-safe animations

3.  **[User Session & Authentication](session_auth.md)**
    *   Active memory session caching and static session managers
    *   Multi-step login transitions and fade transition cross-animations
    *   Database credential auditing and transaction-safe registration alerts isolation

4.  **[Database Schema & Management](database.md)**
    *   SQLite database configurations and entity schemas
    *   Visibility scoping mechanism (Global vs. Local)
    *   Transaction-safe notification isolation for new user creation

5.  **[Static Notification API Reference](notifications_api.md)**
    *   Decoupled static triggering methods
    *   Scope overrides and dispatch behavior on the UI main thread

6.  **[Inventory Subsystem](inventory_system.md)**
    *   Inventory models, data updates, and SQLite persistence
    *   Threshold alert bindings and stock consumption flows
    *   Sanitization rules and regex input validation constraints

7.  **[Automation & Scheduling Subsystem](automation_system.md)**
    *   Operational domain models and automated event tracking
    *   Asynchronous water dispatch logic and schedule seeding
    *   Schedule timing validation regex filters

8.  **[Design & UI Standards](design_standards.md)**
    *   Separation of Concerns (SoC) rules for styling and layouts
    *   Calibrated depth layer definitions and ambient shadows
    *   Translucent overlays and edge-stroke specifications

9.  **[Development & Contribution Guidelines](development_guidelines.md)**
    *   Strict guidelines for objective and technical code comments
    *   Collaborative source-control branching and naming conventions
    *   Build, compilation, and execution procedures using modern build tools
