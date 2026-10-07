# Developer 2 (Frontend Engineer) - Work Phases

> **⚠️ CRITICAL RULE:** Must strictly follow `ui_ux_requirements_specifications.md` at all times. **ZERO extra UI elements** (buttons, menus, actions, inputs, navigation items) beyond what the spec documents.

## Phase 1: Navigation & App Shell
- **1.1 Navigation Graph**: 4 screens only (Dashboard → Upload → Config → QuizView). No additional screens or drawer menus. (`ui/navigation/NavGraph.kt`)
- **1.2 Theme & Design Tokens**: Deep Blue/Indigo primary, Neutral Gray backgrounds. MD3 typography (`HeadlineSmall`, `BodyMedium`). Card radius = 12dp, padding = 16dp. (`ui/theme/Theme.kt`, `Color.kt`, `Type.kt`)
- **1.3 Application Entry**: `MainActivity` with single-activity Compose host. `QuizGenApplication` with Hilt. (`MainActivity.kt`, `QuizGenApplication.kt`)

## Phase 2: Screen 1 — Dashboard / Home Screen
- **2.1 Top App Bar**: Title "AI Quiz Generator" + local storage indicator icon. No settings icon, no overflow menu. (`DashboardScreen.kt`)
- **2.2 Tab Layout**: Exactly 2 tabs ("Generated Quizzes", "Saved Materials"). No additional tabs. (`DashboardScreen.kt`)
- **2.3 Quiz Card**: Title, Difficulty Chip, Question Breakdown, Date Created, "View Exam" button. No edit/share/delete. (`ui/components/QuizCard.kt`)
- **2.4 Material Card**: File Name, Size in MB, Upload Date. No preview/edit/extra action buttons. (`ui/components/MaterialCard.kt`)
- **2.5 FAB**: Single FAB "+ Create New Quiz" (navigates to Screen 2). No speed-dial. (`DashboardScreen.kt`)
- **2.6 Empty State**: Graphic + single CTA button to upload first document. Only when no quizzes/materials exist. (`ui/components/EmptyState.kt`)

## Phase 3: Screen 2 — Upload & Select Material Screen
- **3.1 Upload Zone Card**: Drag-and-drop icon or "Select File from Device" button. Format legend. No camera/URL import. (`UploadScreen.kt`)
- **3.2 Validation Feedback**: Inline Toast/Banner ("File size exceeds 15MB limit"). Progress bar during extraction. No custom dialogs. (`UploadScreen.kt`)
- **3.3 Multi-Select Library List**: Checkbox list of all uploaded materials. No swipe actions/long-press. (`UploadScreen.kt`)
- **3.4 Bottom Sticky Bar**: "Next: Configure Quiz" button (Enabled when >= 1 file selected). No "Skip". (`UploadScreen.kt`)

## Phase 4: Screen 3 — Quiz Configuration Screen
- **4.1 Selected Materials Summary**: Chips showing selected documents with remove (✕). No reorder/add-more inline. (`QuizConfigScreen.kt`)
- **4.2 Difficulty Selector**: 3-way segmented control (Easy | Medium | Hard). No custom difficulty/slider. (`QuizConfigScreen.kt`)
- **4.3 Question Counter Matrix**: 3 counters only (MCQs, Short Questions, Long Questions). Validation (at least 1 > 0). (`QuizConfigScreen.kt`)
- **4.4 Total Summary Banner**: Dynamic total question count. Display only. (`QuizConfigScreen.kt`)
- **4.5 Generate Button**: "Generate Quiz with AI" primary action. Single button. (`QuizConfigScreen.kt`)
- **4.6 Loading Overlay**: Non-cancellable overlay ("Extracting text & generating questions..."). No cancel button. (`ui/components/LoadingOverlay.kt`)

## Phase 5: Screen 4 — Quiz & Answer Key View Screen
- **5.1 Header Details**: Quiz Title, Generated Date, Difficulty Badge, Source File Tags. No edit/share/export. (`QuizViewScreen.kt`)
- **5.2 Filter Chips**: Sticky top: `[All]`, `[MCQs]`, `[Short Qs]`, `[Long Qs]`. Exactly these 4 filters. (`QuizViewScreen.kt`)
- **5.3 Question Card**: Index tag + type badge, full text. For MCQs: options A-D with correct highlighted. Model answer container. Explanation accordion. Read-only, no inputs. (`ui/components/QuestionCard.kt`)

## Phase 6: ViewModel Binding & Polish
- **6.1 Bind ViewModels**: Connect screens to ViewModels. Collect Flows as Compose state.
- **6.2 Status Badges**: Green (`EASY`), Orange (`MEDIUM`), Red (`HARD`). (`ui/components/DifficultyChip.kt`)
- **6.3 Responsive Polish**: Scroll properly, cards render correctly, animations follow MD3.
