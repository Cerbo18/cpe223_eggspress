# Development & Contribution Guidelines

This document outlines environment requirements, compilation instructions, codebase standards, and source control workflows.

## 1. Environment Configurations & Build Commands

The application requires **JDK 25** and **Apache Maven 3.6.0 or higher**.

### Environmental Controls
Ensure the active environmental runtime maps directly to the correct Java standard installation.

*   **Dependency Resolution & Compile Only**:
    ```powershell
    $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"; mvn clean test-compile
    ```
*   **Compile & Execution**:
    ```powershell
    $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"; mvn clean javafx:run
    ```

---

## 2. Technical Code Comments Standard

All inline codebase comments must be written objectively and restrictively to keep files clear of system or design language biases.

### Strict Rules

1. **No Qualitative Adjectives**:
    *   Do not include descriptive buzzwords such as "beautiful", "gorgeous", "premium", "delightful", or "sleek".
2. **Explain the "Why", Not the "What"**:
    *   Detail complex operational logic, thread handling boundaries, layout math, or timing routines.
    *   *Incorrect*: `// Added side margins for standard sleek spacing.`
    *   *Correct*: `// Set spatial margin offset of 24px from the top-right container boundary to prevent overlap with persistent navigation elements.`

---

## 3. Git Branching & Submission Flow

1.  **Pull Primary Updates**:
    ```bash
    git checkout main
    git pull origin main
    ```
2.  **Spawn an Isolated Branch**:
    Use clean action-based feature descriptions:
    ```bash
    git checkout -b feature/your-feature-name
    ```
3.  **Namespace Integrity**:
    *   All newly introduced Java modules must be correctly packed inside `package cpe223.group8.eggspress;` to ensure consistent compile-time modular bindings.
