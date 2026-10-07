# Team Collaboration & AI Agent Execution Protocols

## Project Overview
* **Application:** AI Android Quiz Generator
* **Architecture:** Offline-First Android App (Kotlin, Jetpack Compose, Room DB, Gemini AI API)
* **Storage & Spec Files:**
  * `database_schema_specifications.md`
  * `ui_ux_requirements_specifications.md`

---

## 1. Team Roles & Responsibilities

### Developer 1: Team Lead & Integration Engineer
* **Scope:** Full access to Frontend and Backend codebases. Lead responsible for branch merging, pull request (PR) approvals, and cross-layer integration.
* **Prompt Rule:** **MUST** prefix every prompt with context routing flags:
  * `/backend` — when dealing with database, APIs, domain models, or business logic.
  * `/frontend` — when dealing with Compose UI, screen state, or user interaction components.
* **Core Tasks:**
  * Reviewing code submitted by Dev 2 and Member 3.
  * Merging feature branches into `main` after verification against schema and UI/UX specs.
  * Resolving architectural or state-binding conflicts between frontend and backend layers.

---

### Developer 2: Frontend Engineer
* **Scope:** Full access to Frontend UI layer (`ui/`, screens, Compose components).
* **Tooling:** Uses **Stitch MCP Server** for UI generation, layout prototyping, and screen workflows.
* **Strict Design Constraint:**
  * Must strictly read and follow all business logic from `ui_ux_requirements_specifications.md`.
  * **ZERO EXTRA ELEMENTS:** Do **NOT** add unrequested UI components, buttons, menus, actions, or input fields that are not explicitly documented in the specification file.
* **Core Tasks:**
  * Construct UI layouts for Dashboard, Upload, Config, and Quiz View screens strictly based on `ui_ux_requirements_specifications.md`.
  * Bind ViewModel UI states to Compose views.

---

### Member 3: Backend & Business Logic Engineer
* **Scope:** Full access to Backend, Room Database, API Handling, File Parsers, and Core Business Logic.
* **Governance & Compliance Role:**
  * Continuously inspect Frontend commits/PRs against `ui_ux_requirements_specifications.md`.
  * **Discrepancy Protocol:** If Member 3 identifies *any extra or unapproved elements* (e.g., extra buttons, unexpected navigation, extraneous settings) in the frontend code, Member 3 **MUST automatically trigger an email notification** to Dev 2 containing a clear fixing prompt describing the exact spec deviation and required cleanup.
* **Core Tasks:**
  * Implement Room Database tables, DAOs, and Type Converters (`database_schema_specifications.md`).
  * Build PDF, DOCX, and TXT file parsers with a strict 15MB file-size validator.
  * Manage Gemini API integrations for dynamic quiz generation and answer key formatting.

---

## 2. Multi-Agent Communication & Enforcement Rules

```
                         +-----------------------------+
                         |        Developer 1          |
                         |   (Lead / Integrator)       |
                         | Prefixes: /frontend /backend|
                         +--------------+--------------+
                                        |
               +------------------------+------------------------+
               |                                                 |
               v                                                 v
  +--------------------------+                      +--------------------------+
  |       Developer 2        |                      |         Member 3         |
  |   (Frontend Engineer)    | <--- Auto Email ---  |  (Backend & Specs Audit) |
  | Tool: Stitch MCP Server  |     Fixing Prompt    | Audit UI against Specs   |
  |  Strict Spec Adherence   |                      | Database, Logic & API    |
  +--------------------------+                      +--------------------------+
```

### Protocol Checklist for AI Agents

1. **Routing Verification:**
   * Before executing any query from Developer 1, verify that either `/frontend` or `/backend` is present at the beginning of the prompt. Reject execution if missing.
2. **Strict UI Scope Check:**
   * Before Developer 2 outputs layout code via Stitch MCP, check against `ui_ux_requirements_specifications.md`. Ensure zero extra buttons, options, or controls exist.
3. **Automated Audit Trigger:**
   * When Member 3 reviews frontend code, run a diff comparison against the documented screen elements. If a discrepancy is found, execute the email service prompt immediately to notify Dev 2.

---

## 3. Email Notification Template (Member 3 -> Developer 2)

**Trigger:** Discovered extra/unspecified UI elements in frontend code.

```text
To: dev2@project.local
From: member3@project.local
Subject: [SPEC DISCREPANCY DETECTED] UI Cleanup Required in Screen {Screen_Name}

Hi Dev 2,

During the backend audit of your latest frontend commit, an element discrepancy was detected against ui_ux_requirements_specifications.md.

Discrepancy Details:
- File / Screen: {Screen_Name}
- Identified Deviation: {Description of extra button/element}
- Specification Rule: "No extra buttons or inputs beyond documented specs."

Fixing Prompt for your Agent:
"Remove {Extra Element} from {Screen_Name} to align strictly with ui_ux_requirements_specifications.md. Ensure only documented inputs and standard Material components remain."

Please apply this fix and update the PR.

Best regards,
Member 3
```