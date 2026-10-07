# UI/UX Requirements & Design Specifications

## 1. Overview & Core Principles

The **AI Android Quiz Generator** UI/UX design is built for speed, clarity, and ease of use. It follows standard Android Material Design 3 guidelines to provide a smooth, offline-first experience for creating and viewing quizzes generated from uploaded documents.

### Key UX Principles
* **Direct Task Flows:** Minimal screens between uploading materials and generating an exam.
* **Informative Statuses:** Clear feedback during file parsing, file size validation, and AI prompt execution.
* **Read-Only Clarity:** Easy-to-read view layout for generated questions, options, model answers, and explanations.

---

## 2. Information Architecture & Navigation Map

```
                   +------------------------+
                   |   Screen 1: Dashboard  |
                   |   (Home / History)     |
                   +-----------+------------+
                               |
               +---------------+---------------+
               |                               |
    +----------v----------+         +----------v----------+
    | Screen 2: Upload    |         | Screen 4: Quiz      |
    | & Select Material   |         | View (Generated)    |
    +----------+----------+         +---------------------+
               |
    +----------v----------+
    | Screen 3: Quiz      |
    | Configuration       |
    +---------------------+
```

---

## 3. Screen-by-Screen Specifications

### Screen 1: Dashboard / Home Screen
* **Primary Goal:** Display history of previously generated quizzes and saved library files stored locally.
* **UI Elements:**
  * **Top App Bar:** Title ("AI Quiz Generator") and local storage indicator icon.
  * **Tab Layout (Segmented Control):**
    * **Tab 1: Generated Quizzes:** Displays cards for previously generated exams.
      * *Card Components:* Quiz Title, Difficulty Chip (`EASY`, `MEDIUM`, `HARD`), Question Breakdown (e.g., "10 MCQs, 3 Short, 1 Long"), Date Created, and a "View Exam" button.
    * **Tab 2: Saved Materials:** Displays uploaded files (PDF, DOCX, TXT) with metadata (File Name, Size in MB, Upload Date).
  * **Floating Action Button (FAB):** "+ Create New Quiz" (Navigates to Screen 2).
  * **Empty State Graphic:** Displayed when no quizzes or materials exist with a direct call-to-action button to upload first document.

---

### Screen 2: Upload & Select Material Screen
* **Goal:** Allow users to upload new files (PDF, DOCX, TXT <= 15MB) or select existing uploaded materials for the new quiz.
* **UI Elements:**
  * **Upload Zone:** 
    * Card with drag-and-drop icon or "Select File from Device" button.
    * Supported Formats Legend (`PDF`, `DOCX`, `TXT` - Max 15MB).
  * **Parsing & Validation Feedback:**
    * Inline Toast / Banner Error: *"File size exceeds 15MB limit"* if validation fails.
    * Progress bar showing text extraction phase.
  * **Multi-Select Library List:**
    * Checkbox item list showing all uploaded materials saved in the Room DB.
    * Allows selecting single or multiple files to combine into a single quiz generation batch.
  * **Bottom Sticky Bar:** "Next: Configure Quiz" button (Enabled only when >= 1 file is selected).

---

### Screen 3: Quiz Configuration Screen
* **Goal:** Set quiz difficulty and exact question counts across multiple types.
* **UI Elements:**
  * **Selected Materials Summary:** Chips showing selected documents (e.g., `[Chapter1.pdf ✕]` `[Notes.txt ✕]`).
  * **Difficulty Selector:** 3-Way Segmented Control Segment (`Easy` | `Medium` | `Hard`).
  * **Question Types Counter Matrix:**
    * **MCQs Counter:** `[-]  10  [+]`
    * **Short Questions Counter:** `[-]  3  [+]`
    * **Long Questions Counter:** `[-]  1  [+]`
    * *Validation:* At least 1 question type count must be > 0.
  * **Total Questions Summary Banner:** Dynamic calculation of overall question count.
  * **Primary Action Button:** "Generate Quiz with AI" button.
  * **Loading Overlay / Modal:** Non-cancellable overlay during AI request showing progress status: *"Extracting text & generating questions..."*

---

### Screen 4: Quiz & Answer Key View Screen (Read-Only)
* **Goal:** Read-only inspection of generated questions, choices, model answers, and explanations.
* **UI Elements:**
  * **Header Details:** Quiz Title, Generated Date, Difficulty Badge, and Source File Tags.
  * **Filter Chips (Sticky Top Bar):** Quick filter by category: `[ All ]`, `[ MCQs ]`, `[ Short Qs ]`, `[ Long Qs ]`.
  * **Question Card Item:**
    * Question Index Tag & Type Badge (`MCQ`, `SHORT`, `LONG`).
    * Full Question Text Prompt.
    * *For MCQs:* Options List (A, B, C, D) with the correct answer visually highlighted in a distinct container.
    * **Model Answer Container:** Highlighted box with the AI-generated model answer/key.
    * **Explanation Accordion / Card:** Detailed AI justification/rationale explaining the answer.

---

## 4. Design Guidelines & Component System

| Component Type | Usage Guidelines |
| :--- | :--- |
| **Primary Color Palette** | Deep Blue / Indigo for top bar and primary CTAs; Neutral Gray backgrounds. |
| **Status Badges** | Green (`EASY`), Orange (`MEDIUM`), Red (`HARD`) for difficulty levels. |
| **Typography** | Material Design 3 standard typography (`HeadlineSmall` for titles, `BodyMedium` for questions). |
| **Card Layouts** | Elevated cards with rounded corners (12dp radius) and standard padding (16dp). |

---

## 5. User Interaction Rules & Validation

1. **File Size Limit:** Direct verification before uploading to storage. File parsing cancels immediately if size exceeds 15.0 MB.
2. **Offline Viewing:** Generated quizzes stored in local DB remain completely readable offline without network access.
3. **Immutable History:** Generated questions are strictly read-only and preserved as created.