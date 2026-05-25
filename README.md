# Eggspress Chicken Farm Manager

![Eggspress Chicken Farm Manager Logo](./src/main/resources/kaviyes/nhx/eggspress/Assets/1x/Eggspress-Combination-Mark-Full.png)

> [!WARNING]
> This repository is created for educational use only.

Welcome to Eggspress Chicken Farm Manager. For coding simplicity in packages, directories, and Maven configuration, the project utilizes the identifier "eggspress". This application is a modular, high-performance farm management platform utilizing JavaFX for its interactive graphical user interface, SQLite for secure, lightweight local data persistence, and a modern, high-contrast style system built on calibrated depth layers.

---

## System Requirements and Prerequisites

To build, run, and contribute to this repository, your local development system must meet the following software requirements:

*   **Java Development Kit (JDK)**: Version 25 (LTS release is highly recommended, utilizing compile-time and runtime preview options)
*   **Apache Maven**: Version 3.6.0 or higher
*   **Operating System**: Windows 10/11 (with PowerShell or Command Prompt)
*   **SQLite**: Local JDBC client (handled automatically via Maven dependencies)

---

## Environment Setup Instructions

We recommend using Scoop, a modern command-line installer for Windows, to easily set up and manage your development dependencies.

### 1. Installing Scoop (If not already installed)
Open PowerShell (with administrator or standard user context) and run the following commands:

```powershell
# Set script execution policy for the current user session
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Download and run the Scoop installer script
irm get.scoop.sh | iex
```

### 2. Setting up JDK and Apache Maven via Scoop
Once Scoop is installed, execute the following commands to install your Java environment and build tools:

```powershell
# Add the official Java bucket to Scoop
scoop bucket add java 

# Install OpenJDK 25
scoop install java/openjdk25 # Ignore if LTS version of Java is already installed 

# Install Apache Maven
scoop install main/maven
```

### 3. Verification
Verify your system environment paths have been successfully updated by running:

```powershell
# Verify Java installation
java -version

# Verify Maven installation
mvn -version
```

---

## How to Clone the Repository

To clone this repository and download all project files, open your terminal (PowerShell, Command Prompt, or Git Bash) and execute the following commands:

```bash
# Clone the repository
git clone https://github.com/kvedux/cpe223_eggspress.git

# Navigate into the project root directory
cd cpe223_eggspress
```

---

## Project Architecture and Directories

```text
eggspress/
│
├── config/                         # App configurations and database drivers
│   └── database.properties         # SQLite connection settings and credentials
│
├── src/
│   └── main/
│       ├── java/
│       │   └── cpe223/
│       │       └── group8/
│       │           └── eggspress/
│       │               ├── Main.java    # Application entry point (JavaFX Starter)
│       │               │
│       │               ├── config/      # Database driver connections and schema setup
│       │               │   └── DatabaseConfig.java
│       │               │
│       │               ├── models/      # Domain Logic / Entity models
│       │               │   ├── User.java
│       │               │   ├── ChickenHouse.java
│       │               │   ├── FeedingSchedule.java
│       │               │   ├── InventoryItem.java
│       │               │   ├── Automation.java
│       │               │   └── Notification.java
│       │               │
│       │               ├── repository/  # Data Access Object (DAO) CRUD interactions
│       │               │   ├── BaseRepository.java
│       │               │   ├── UserRepository.java
│       │               │   ├── FarmRepository.java
│       │               │   └── NotificationRepository.java
│       │               │
│       │               ├── services/    # App-level services (session & async syncing)
│       │               │   ├── SessionManager.java
│       │               │   ├── NotificationListener.java
│       │               │   └── NotificationService.java
│       │               │
│       │               └── controllers/ # Glue layer between views and models
│       │                   ├── LoginController.java
│       │                   ├── DashboardController.java
│       │                   ├── LayoutController.java
│       │                   ├── InventoryController.java
│       │                   ├── AcountMgmtController.java
│       │                   ├── AutomationController.java
│       │                   └── OverviewController.java
│       │
│       └── resources/               # Static markup layouts, UI styles, and brand assets
│           ├── cpe223/
│           │   └── group8/
│           │       └── eggspress/
│           │           ├── views/       # JavaFX FXML screen templates
│           │           │   ├── login.fxml
│           │           │   ├── dashboard.fxml
│           │           │   ├── layout.fxml
│           │           │   ├── inventory.fxml
│           │           │   ├── acountMgmt.fxml
│           │           │   ├── automation.fxml
│           │           │   └── overview.fxml
│           │           │
│           │           ├── css/         # Modular CSS system
│           │           │   ├── global.css          # Baseline application-wide styles
│           │           │   ├── light.css           # Global light theme colors
│           │           │   ├── dark.css            # Global dark theme colors
│           │           │   ├── acountMgmt/         # Account Management-specific CSS
│           │           │   │   ├── acountMgmt.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   ├── automation/         # Automation panel-specific CSS
│           │           │   │   ├── automation.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   ├── dashboard/          # Dashboard-specific CSS
│           │           │   │   ├── dashboard.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   ├── inventory/          # Inventory-specific CSS
│           │           │   │   ├── inventory.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   ├── layout/             # Window chrome & navigation CSS
│           │           │   │   ├── layout.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   ├── login/              # Login interface-specific CSS
│           │           │   │   ├── login.css
│           │           │   │   ├── light.css
│           │           │   │   └── dark.css
│           │           │   └── overview/           # Overview sub-view CSS
│           │           │       ├── overview.css
│           │           │       └── light.css
│           │           │
│           │           └── icons/       # Application window icons
│           │               └── icon.png
│           │
│           └── kaviyes/                 # Brand and design assets
│               └── nhx/
│                   └── eggspress/
│                       └── Assets/      # Vector and scaled raster brand marks
│                           ├── COPYRIGHT
│                           ├── 1x/      # 1x PNG branding assets
│                           │   ├── Eggspress-App-Icon.png
│                           │   ├── Eggspress-Combination-Mark-Full.png
│                           │   ├── Eggspress-Combination-Mark.png
│                           │   ├── Eggspress-Icon.png
│                           │   └── Wordmark.png
│                           ├── SVG/     # SVG Vector branding assets
│                           │   ├── Eggspress App Icon.svg
│                           │   ├── Eggspress Combination Mark Full.svg
│                           │   ├── Eggspress Combination Mark.svg
│                           │   ├── Eggspress Icon.svg
│                           │   └── Wordmark.svg
│                           └── ...      # Scaled assets (0.2x, 0.5x, 16w, etc.)
│
├── database/                        # Local database persistence
│   └── eggspress.db
│
├── .gitignore                       # Version-control filters
└── pom.xml                          # Maven build dependencies (JDK 25 / JavaFX 25)
```

---

## Execution and Compilation

The application is configured to leverage compile-time and runtime preview features in **Java 25** and **OpenJFX 25**. Make sure that your `JAVA_HOME` environment variable points to a valid JDK 25 installation.

### 1. Build and Compile
Fetches JavaFX and SQLite modules, resolves all libraries, and compiles the source code with preview options:
```powershell
# Windows (PowerShell) - Compile and resolve dependencies
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"; mvn clean compile
```

### 2. Run Application
Launches the JavaFX graphics UI environment:
```powershell
# Windows (PowerShell) - Run the Application
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"; mvn clean javafx:run
```

---

## Technical Features & Architectural Implementations

### A. Accessible Static Notification API
To decoupling notification triggers from context fetches and keep developer code concise and readable, `NotificationService` exposes a simple static API:
*   `NotificationService.notificationInfo(String message)` (Default local scope context)
*   `NotificationService.notificationInfo(String message, boolean isGlobal)`
*   `NotificationService.notificationWarning(String message)` (Default global scope context)
*   `NotificationService.notificationWarning(String message, boolean isGlobal)`
*   `NotificationService.notificationCritical(String message)` (Default global scope context)
*   `NotificationService.notificationCritical(String message, boolean isGlobal)`

These helpers automatically persist the alerts and immediately dispatch updates on the JavaFX application thread.

### B. Global vs. Local Visibility Scopes
Notifications are categorized by their visibility scope:
1.  **Global Scoped (`isGlobal = true`)**: Sent to all users. Saved in the SQLite database with a `NULL` username. Visible in every account's notification drawer.
2.  **Local Scoped (`isGlobal = false`)**: Sent to a specific user (e.g., input validation failures, user actions). Saved in the database mapped to the current `SessionManager` username. Correctly ignored on the fly by other accounts' dashboard pollers.

### C. Legacy Notification Isolation for New Accounts
When a new user registers via `UserRepository`, a database transaction automatically inserts read/cleared states into the `user_notification_states` table for **all** pre-existing database notifications. This guarantees that new user profiles begin with a perfectly empty notification tray on creation, while correctly receiving all subsequent notifications.

---

## Project Coding Guidelines & Development Standards

### 1. Objective & Technical Code Comments
To maintain a high-quality, professional codebase, all code documentation and inline comments must remain strictly technical and objective:
*   **No Brand / System References**: Do not use proprietary names like *Apple*, *iOS*, *macOS*, *SwiftUI*, *Windows*, *Fluent*, or *HIG*. Refer instead to physical visual properties (e.g., "translucent overlays", "glassmorphic materials", "drop shadows", or "ambient layers").
*   **No Subjective Style Adjectives**: Refrain from using qualitative buzzwords like "premium", "beautiful", "gorgeous", or "sleek".
*   **Why, Not What**: Explain architectural details, layout math, thread-safety conditions, or spatial offset boundaries.
    *   *Bad*: `// Standard HIG/Fluent consistency margins for a premium, beautiful look.`
    *   *Good*: `// Set spatial margin offset of 24px from the top-right container boundary to prevent overlap with persistent navigation elements.`

### 2. Strict Separation of Concerns (SoC)
*   **Styling Isolation**: Absolutely **no** inline styling in Java controller files using `node.setStyle(...)` or programmatic visual overrides. All visual properties, margins, hover actions, colors, and elevations must reside in the component CSS stylesheets under `resources/css/`. Java classes interact with styling strictly by adding, removing, or toggling CSS style classes via `node.getStyleClass().add("class-name")`.
*   **Database Isolation**: All SQLite connection logic is centralized within `DatabaseConfig.java`. Do not write raw JDBC connection strings inside your views or controllers. Always execute queries through repositories inheriting from `BaseRepository`.
*   **Layout and Controller Binding**: Assign FXML views under `views/` to their corresponding controllers under `controllers/` using the `fx:controller` XML declaration.

### 3. Microsoft Fluent 2 Elevation Standard
The application follows the Fluent 2 depth hierarchy standard using opaque high-contrast background surfaces paired with calibrated dropshadow tokens to separate overlapping components:
*   **Elevation 4 (Card / Push Toast)**: Uses dropshadow `dropshadow(gaussian, rgba(0, 0, 0, 0.14), 8, 0, 0, 2)` (Light Mode) or `dropshadow(gaussian, rgba(0, 0, 0, 0.28), 8, 0, 0, 2)` (Dark Mode) with an `8px` corner radius.
*   **Elevation 8 (Notification Panel / Flyout)**: Uses dropshadow `dropshadow(gaussian, rgba(0, 0, 0, 0.14), 16, 0, 0, 4)` (Light Mode) or `dropshadow(gaussian, rgba(0, 0, 0, 0.28), 16, 0, 0, 4)` (Dark Mode) with an `8px` corner radius.

---

## Collaborative Workflow (Creating a Pull Request)

To submit your code changes to the project, follow the official Git branching and Pull Request (PR) workflow:

### Step 1: Pull the Latest Changes
```bash
# Switch to the main branch
git checkout main

# Retrieve and merge the latest code
git pull origin main
```

### Step 2: Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### Step 3: Code and Commit Changes
```bash
# Check which files were modified
git status

# Stage all changes for commit
git add .

# Save the changes with a clear summary
git commit -m "Implement validation checks on the login screen"
```

### Step 4: Push to GitHub
```bash
git push origin feature/your-feature-name
```

### Step 5: Submit a Pull Request
1. Open your web browser and navigate to: `https://github.com/kvedux/cpe223_eggspress`
2. Click **Compare & pull request** on the yellow notification banner (or open the **Pull requests** tab, then **New pull request**).
3. Review your diffs, input a descriptive title, and outline your changes.
4. Click **Create pull request**. Once team members review and approve it, it can be merged directly into `main`.
```,StartLine:1,TargetContent:
