# Development Work Phases — AI Android Quiz Generator

---

## Part A: Member 3 — Backend & Business Logic Engineer

### Phase 1: Project Scaffolding & Room Database Foundation

**Goal:** Set up the Android project structure and implement the complete Room database layer.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 1.1 | **Project Setup** | Create Android project (`com.app.quizgen`), configure Gradle with Room, Hilt, Coroutines, Kotlin KSP, and Gemini SDK dependencies. | `build.gradle.kts` (app + root), `settings.gradle.kts` |
| 1.2 | **Room Entities** | Implement all 4 entities exactly per `database_schema_specifications.md`: `StudyMaterialEntity`, `QuizEntity`, `QuizMaterialCrossRef`, `QuestionEntity`. | `data/local/entity/*.kt` |
| 1.3 | **Type Converters** | Create converters for JSON ↔ List<String> (for `options_json`) and any other needed serializations. | `data/local/converter/Converters.kt` |
| 1.4 | **Relational Data Classes** | Implement `QuizWithQuestionsAndMaterials` with proper `@Embedded` + `@Relation` + `@Junction` annotations. | `data/local/relation/*.kt` |
| 1.5 | **Room Database Class** | Create the `AppDatabase` abstract class registering all entities, converters, and DAO references. DB name: `ai_quiz_generator.db`. | `data/local/AppDatabase.kt` |

---

### Phase 2: DAOs & Repository Layer

**Goal:** Build the data access layer and repository abstractions.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 2.1 | **StudyMaterialDao** | CRUD operations: insert, getAll, getById, delete. Query by file type. | `data/local/dao/StudyMaterialDao.kt` |
| 2.2 | **QuizDao** | Insert quiz (return ID), getAll (ordered by `created_at` DESC), getById, delete (CASCADE triggers). | `data/local/dao/QuizDao.kt` |
| 2.3 | **QuestionDao** | Insert batch questions, getByQuizId, getByQuizIdAndType (filter by `MCQ`/`SHORT`/`LONG`). | `data/local/dao/QuestionDao.kt` |
| 2.4 | **CrossRefDao** | Insert cross-refs, query materials for a quiz, query quizzes for a material. | `data/local/dao/QuizMaterialCrossRefDao.kt` |
| 2.5 | **Repository Interfaces** | Create `StudyMaterialRepository`, `QuizRepository` abstractions exposing suspend + Flow-based APIs. | `data/repository/*.kt` |
| 2.6 | **Repository Implementations** | Implement repositories using DAOs, wrapping DB transactions where needed (e.g., creating a quiz + questions + cross-refs atomically). | `data/repository/impl/*.kt` |

---

### Phase 3: File Parsers & Upload Validation

**Goal:** Implement document ingestion with strict 15MB validation and text extraction.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 3.1 | **File Size Validator** | Utility that checks file size ≤ 15.0 MB **before** any storage or parsing begins. Returns error result if exceeded. | `util/FileValidator.kt` |
| 3.2 | **PDF Parser** | Extract plain text from PDF files using a lightweight library (e.g., `iText` or `PdfBox-Android`). | `data/parser/PdfTextExtractor.kt` |
| 3.3 | **DOCX Parser** | Extract plain text from `.docx` files using Apache POI (lightweight variant) or similar. | `data/parser/DocxTextExtractor.kt` |
| 3.4 | **TXT Parser** | Read raw text content from `.txt` files with charset detection. | `data/parser/TxtTextExtractor.kt` |
| 3.5 | **Parser Factory** | Unified `FileParserFactory` that dispatches to the correct parser based on `file_type` (PDF/DOCX/TXT). | `data/parser/FileParserFactory.kt` |
| 3.6 | **File Storage Manager** | Copy uploaded file to app-internal storage (`Android/data/com.app.quizgen/files`), return the local `file_uri`. | `data/storage/FileStorageManager.kt` |

---

### Phase 4: Gemini AI Integration & Quiz Generation Logic

**Goal:** Build the AI prompt pipeline that generates structured quiz data from extracted text.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 4.1 | **Gemini API Client** | Configure the Gemini generative AI SDK client with the API key and model selection. | `data/remote/GeminiClient.kt` |
| 4.2 | **Prompt Builder** | Construct dynamic prompts based on: extracted text, difficulty level (`EASY`/`MEDIUM`/`HARD`), and question counts (`mcq_count`, `short_q_count`, `long_q_count`). Prompt must instruct the AI to return structured JSON. | `data/remote/PromptBuilder.kt` |
| 4.3 | **Response Parser** | Parse the AI's JSON response into a list of `QuestionEntity` objects. Handle malformed responses gracefully with retry/fallback. | `data/remote/GeminiResponseParser.kt` |
| 4.4 | **Quiz Generation Use Case** | Orchestrate the full flow: validate inputs → combine extracted texts → build prompt → call Gemini → parse response → save Quiz + Questions + CrossRefs in a single DB transaction. | `domain/usecase/GenerateQuizUseCase.kt` |

---

### Phase 5: ViewModels & Domain Wiring

**Goal:** Expose backend logic to the UI layer through ViewModels and dependency injection.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 5.1 | **Hilt DI Module** | Provide Room DB, DAOs, repositories, parsers, and Gemini client via Hilt dependency injection. | `di/AppModule.kt`, `di/DatabaseModule.kt` |
| 5.2 | **DashboardViewModel** | Expose `Flow<List<QuizEntity>>` and `Flow<List<StudyMaterialEntity>>` for the dashboard tabs. Handle quiz deletion. | `ui/viewmodel/DashboardViewModel.kt` |
| 5.3 | **UploadViewModel** | Handle file picking, validation (15MB), parsing, storage, and inserting `StudyMaterialEntity`. Expose upload progress state. | `ui/viewmodel/UploadViewModel.kt` |
| 5.4 | **QuizConfigViewModel** | Accept selected material IDs + config (difficulty, counts). Trigger `GenerateQuizUseCase`. Expose generation status (loading/success/error). | `ui/viewmodel/QuizConfigViewModel.kt` |
| 5.5 | **QuizViewViewModel** | Load `QuizWithQuestionsAndMaterials` by quiz ID. Expose filter state (All/MCQ/SHORT/LONG). | `ui/viewmodel/QuizViewViewModel.kt` |

---

### Phase 6: Frontend Audit & Integration Support

**Goal:** Audit Developer 2's frontend code and support final integration.

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 6.1 | **UI Spec Audit** | Diff every Compose screen against `ui_ux_requirements_specifications.md`. Flag any extra buttons, menus, inputs, or navigation not documented in the spec. | Audit report / Email to Dev 2 |
| 6.2 | **Integration Testing** | Verify ViewModel → Repository → DAO → DB flow end-to-end. Test cascade deletes, transaction atomicity, and empty states. | Test results |
| 6.3 | **Edge Case Handling** | Validate: oversized files rejected, empty text extraction handled, Gemini API timeouts retried, malformed JSON responses caught. | Bug fixes / hardening |

---
---

## Part B: Developer 2 — Frontend Engineer

> **⚠️ CRITICAL RULE:** You must strictly follow `ui_ux_requirements_specifications.md` at all times. **ZERO extra UI elements** (buttons, menus, actions, inputs, navigation items) beyond what the spec documents. Any deviation will be flagged by Member 3 and you will be required to remove it.

### Phase 1: Navigation & App Shell

| # | Task | Strict Spec Reference | Deliverable |
|---|------|-----------------------|-------------|
| 1.1 | **Navigation Graph** | 4 screens only: Dashboard → Upload → Config → QuizView. No additional screens or drawer menus. | `ui/navigation/NavGraph.kt` |
| 1.2 | **Theme & Design Tokens** | Deep Blue/Indigo primary, Neutral Gray backgrounds. MD3 typography (`HeadlineSmall` titles, `BodyMedium` questions). Card radius = 12dp, padding = 16dp. | `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` |
| 1.3 | **Application Entry** | `MainActivity` with single-activity Compose host. `QuizGenApplication` with Hilt. | `MainActivity.kt`, `QuizGenApplication.kt` |

---

### Phase 2: Screen 1 — Dashboard / Home Screen

| # | Task | Strict Spec Reference | Deliverable |
|---|------|-----------------------|-------------|
| 2.1 | **Top App Bar** | Title: "AI Quiz Generator" + local storage indicator icon. **No settings icon, no overflow menu.** | Part of `DashboardScreen.kt` |
| 2.2 | **Tab Layout** | Exactly 2 tabs: "Generated Quizzes" and "Saved Materials". **No additional tabs.** | Part of `DashboardScreen.kt` |
| 2.3 | **Quiz Card** | Show: Quiz Title, Difficulty Chip (Green/Orange/Red), Question Breakdown text, Date Created, "View Exam" button. **No edit, share, or delete buttons on cards.** | `ui/components/QuizCard.kt` |
| 2.4 | **Material Card** | Show: File Name, Size in MB, Upload Date. **No preview, edit, or extra action buttons.** | `ui/components/MaterialCard.kt` |
| 2.5 | **FAB** | Single FAB: "+ Create New Quiz" → navigates to Screen 2. **No speed-dial, no secondary FAB.** | Part of `DashboardScreen.kt` |
| 2.6 | **Empty State** | Graphic + single CTA button to upload first document. **Only when no quizzes/materials exist.** | `ui/components/EmptyState.kt` |

---

### Phase 3: Screen 2 — Upload & Select Material Screen

| # | Task | Strict Spec Reference | Deliverable |
|---|------|-----------------------|-------------|
| 3.1 | **Upload Zone Card** | Drag-and-drop icon or "Select File from Device" button. Format legend: `PDF, DOCX, TXT - Max 15MB`. **No camera, URL import, or cloud buttons.** | Part of `UploadScreen.kt` |
| 3.2 | **Validation Feedback** | Inline Toast/Banner: "File size exceeds 15MB limit". Progress bar during text extraction. **No custom dialogs or extra warnings.** | Part of `UploadScreen.kt` |
| 3.3 | **Multi-Select Library List** | Checkbox list of all uploaded materials from Room DB. Single or multi-select. **No swipe actions, no long-press menus.** | Part of `UploadScreen.kt` |
| 3.4 | **Bottom Sticky Bar** | "Next: Configure Quiz" button. Enabled only when ≥ 1 file selected. **No "Skip" or extra buttons.** | Part of `UploadScreen.kt` |

---

### Phase 4: Screen 3 — Quiz Configuration Screen

| # | Task | Strict Spec Reference | Deliverable |
|---|------|-----------------------|-------------|
| 4.1 | **Selected Materials Summary** | Chips showing selected documents with remove (✕). **No reorder, no add-more inline.** | Part of `QuizConfigScreen.kt` |
| 4.2 | **Difficulty Selector** | 3-way segmented control: Easy \| Medium \| Hard. **No custom difficulty or slider.** | Part of `QuizConfigScreen.kt` |
| 4.3 | **Question Counter Matrix** | 3 counters only: MCQs `[-] N [+]`, Short Questions `[-] N [+]`, Long Questions `[-] N [+]`. Validation: at least 1 count > 0. **No "True/False", "Fill-in-Blank", or extra types.** | Part of `QuizConfigScreen.kt` |
| 4.4 | **Total Summary Banner** | Dynamic total question count. **Display only, no interactive elements.** | Part of `QuizConfigScreen.kt` |
| 4.5 | **Generate Button** | "Generate Quiz with AI" primary action. **Single button, no "Save Draft" or extras.** | Part of `QuizConfigScreen.kt` |
| 4.6 | **Loading Overlay** | Non-cancellable overlay: "Extracting text & generating questions..." **No cancel button.** | `ui/components/LoadingOverlay.kt` |

---

### Phase 5: Screen 4 — Quiz & Answer Key View Screen

| # | Task | Strict Spec Reference | Deliverable |
|---|------|-----------------------|-------------|
| 5.1 | **Header Details** | Quiz Title, Generated Date, Difficulty Badge, Source File Tags. **No edit title, share, or export buttons.** | Part of `QuizViewScreen.kt` |
| 5.2 | **Filter Chips** | Sticky top: `[All]`, `[MCQs]`, `[Short Qs]`, `[Long Qs]`. **Exactly these 4 filters, no extras.** | Part of `QuizViewScreen.kt` |
| 5.3 | **Question Card** | Index tag + type badge, full question text. For MCQs: options A-D with correct answer highlighted. Model answer container. Explanation accordion/card. **Read-only, no answer input fields, no scoring.** | `ui/components/QuestionCard.kt` |

---

### Phase 6: ViewModel Binding & Polish

| # | Task | Details | Deliverable |
|---|------|---------|-------------|
| 6.1 | **Bind ViewModels** | Connect each screen to its corresponding ViewModel (provided by Member 3). Collect Flows as Compose state. | Updated screen files |
| 6.2 | **Status Badges** | Green chip for `EASY`, Orange for `MEDIUM`, Red for `HARD`. | `ui/components/DifficultyChip.kt` |
| 6.3 | **Responsive Polish** | Ensure all screens scroll properly, cards render correctly on different screen sizes, and animations follow MD3 guidelines. | Updated screen files |

---
---

## Dependency Chart

```
Member 3 Phase 1 (DB)  ──────►  Member 3 Phase 2 (DAOs/Repos)
                                         │
                                         ▼
                               Member 3 Phase 3 (Parsers)
                                         │
                                         ▼
                               Member 3 Phase 4 (Gemini AI)
                                         │
                                         ▼
                               Member 3 Phase 5 (ViewModels)  ◄──── Dev 2 Phase 1–5
                                         │                           (can run in parallel)
                                         ▼
                               Member 3 Phase 6 (Audit)  ◄────────── Dev 2 Phase 6
                               Dev 2 Phase 6 (Binding)               (ViewModel wiring)
```

> **Dev 2 Phases 1–5** (UI layout) can proceed **in parallel** with Member 3's backend work, using mock/stub data. Once Member 3 delivers ViewModels (Phase 5), Dev 2 wires them in Phase 6.

---

## Enforcement Reminders

- **Member 3:** After Dev 2 submits any PR, audit every screen element against `ui_ux_requirements_specifications.md`. If **any** spec deviation is found, send the email notification using the template in `team_collaboration_rules.md`.
- **Developer 2:** Before every commit, self-check: *"Does my screen have ANY element not listed in the spec?"* If yes, remove it before pushing.
- **Developer 1:** Merge PRs only after Member 3 has signed off on spec compliance.
