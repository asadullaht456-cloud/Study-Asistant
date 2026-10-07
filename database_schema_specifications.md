# Database & Schema Specifications

## 1. Overview & Architecture

The **AI Android Quiz Generator** uses a purely local storage strategy leveraging Android's **Room Persistence Library** over SQLite. All user data, study materials, generated quizzes, questions, model answers, and explanations remain entirely on the local device for full offline availability.

### Storage Strategy
* **Database File:** `ai_quiz_generator.db`
* **Local Meta-data & Structured Data:** Managed by Room Database.
* **File Storage:** Uploaded files (PDF, DOCX, TXT) are kept in the app-specific internal storage directory (`Android/data/com.app.quizgen/files`), with their local file URIs and parsed plain text stored within the database.

---

## 2. Entity Relationship (ER) Strategy

```
 +------------------+          +------------------------+          +-------------------+
 |   StudyMaterial  |          |   QuizMaterialCrossRef |          |       Quiz        |
 +------------------+          +------------------------+          +-------------------+
 | PK: material_id  | <------->| PK, FK: material_id    |<-------->| PK: quiz_id       |
 | title            |          | PK, FK: quiz_id        |          | title             |
 | file_type        |          +------------------------+          | difficulty_level  |
 | file_uri         |                                              | total_questions   |
 | extracted_text   |                                              | created_at        |
 | file_size_mb     |                                              +-------------------+
 | uploaded_at      |                                                        |
 +------------------+                                                        | 1
                                                                             |
                                                                             | N
                                                                   +-------------------+
                                                                   |     Question      |
                                                                   +-------------------+
                                                                   | PK: question_id   |
                                                                   | FK: quiz_id       |
                                                                   | question_type     |
                                                                   | question_text     |
                                                                   | options_json      |
                                                                   | correct_answer    |
                                                                   | explanation       |
                                                                   +-------------------+
```

### Key Relationships
1. **`StudyMaterial` to `Quiz` (Many-to-Many):** Handled via the `QuizMaterialCrossRef` junction table. A single quiz can be generated using content combined from multiple study materials.
2. **`Quiz` to `Question` (One-to-Many):** A single generated quiz contains multiple questions. When a quiz is deleted, all associated questions are deleted automatically via `CASCADE`.

---

## 3. Detailed Entity Schemas

### Entity 1: `StudyMaterial`
Stores metadata and parsed text content from files uploaded by the user.

* **Table Name:** `study_materials`

| Column Name | SQLite Data Type | Room / Kotlin Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `material_id` | `INTEGER` | `Long` | `PRIMARY KEY`, `AUTOINCREMENT` | Unique identifier for the material |
| `title` | `TEXT` | `String` | `NOT NULL` | Original filename / Display title |
| `file_type` | `TEXT` | `String` | `NOT NULL` | Extension type: `PDF`, `DOCX`, `TXT` |
| `file_uri` | `TEXT` | `String` | `NOT NULL` | App internal URI path to local file |
| `extracted_text` | `TEXT` | `String` | `NOT NULL` | Parsed plain text content passed to AI |
| `file_size_mb` | `REAL` | `Double` | `NOT NULL` | File size check (Max 15.0 MB) |
| `uploaded_at` | `INTEGER` | `Long` | `NOT NULL` | Epoch timestamp of upload date |

---

### Entity 2: `Quiz`
Stores overall metadata and configurations for each generated quiz or exam.

* **Table Name:** `quizzes`

| Column Name | SQLite Data Type | Room / Kotlin Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `quiz_id` | `INTEGER` | `Long` | `PRIMARY KEY`, `AUTOINCREMENT` | Unique identifier for the quiz |
| `title` | `TEXT` | `String` | `NOT NULL` | User-defined or auto-generated title |
| `difficulty_level` | `TEXT` | `String` | `NOT NULL` | Enum: `EASY`, `MEDIUM`, `HARD` |
| `total_questions` | `INTEGER` | `Int` | `NOT NULL` | Total count of all generated questions |
| `mcq_count` | `INTEGER` | `Int` | `NOT NULL`, `DEFAULT 0` | Requested number of MCQs |
| `short_q_count` | `INTEGER` | `Int` | `NOT NULL`, `DEFAULT 0` | Requested number of Short Questions |
| `long_q_count` | `INTEGER` | `Int` | `NOT NULL`, `DEFAULT 0` | Requested number of Long Questions |
| `created_at` | `INTEGER` | `Long` | `NOT NULL` | Epoch timestamp of generation date |

---

### Entity 3: `QuizMaterialCrossRef` (Junction Entity)
Maps the many-to-many relationship between `Quiz` and `StudyMaterial`.

* **Table Name:** `quiz_material_cross_ref`
* **Composite Primary Key:** (`quiz_id`, `material_id`)

| Column Name | SQLite Data Type | Room / Kotlin Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `quiz_id` | `INTEGER` | `Long` | `FK -> Quiz(quiz_id) ON DELETE CASCADE` | Reference to parent Quiz |
| `material_id` | `INTEGER` | `Long` | `FK -> StudyMaterial(material_id) ON DELETE CASCADE` | Reference to source Material |

---

### Entity 4: `Question`
Stores individual questions, options, model answers, and explanations.

* **Table Name:** `questions`

| Column Name | SQLite Data Type | Room / Kotlin Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `question_id` | `INTEGER` | `Long` | `PRIMARY KEY`, `AUTOINCREMENT` | Unique identifier for the question |
| `quiz_id` | `INTEGER` | `Long` | `FK -> Quiz(quiz_id) ON DELETE CASCADE` | Foreign Key referencing parent Quiz |
| `question_type` | `TEXT` | `String` | `NOT NULL` | Enum: `MCQ`, `SHORT`, `LONG` |
| `question_text` | `TEXT` | `String` | `NOT NULL` | The full prompt or question text |
| `options_json` | `TEXT` | `String?` | `NULLABLE` | JSON array of options (e.g. `["A","B","C","D"]`) for MCQs; `NULL` for Short/Long |
| `correct_answer` | `TEXT` | `String` | `NOT NULL` | Model answer or option key generated by AI |
| `explanation` | `TEXT` | `String?` | `NULLABLE` | Detailed step-by-step AI explanation |

---

## 4. Kotlin Data Access Objects (DAO) & Room Data Structures

### Room Data Class Declarations

```kotlin
import androidx.room.*

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val material_id: Long = 0,
    val title: String,
    val file_type: String,
    val file_uri: String,
    val extracted_text: String,
    val file_size_mb: Double,
    val uploaded_at: Long
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val quiz_id: Long = 0,
    val title: String,
    val difficulty_level: String,
    val total_questions: Int,
    val mcq_count: Int = 0,
    val short_q_count: Int = 0,
    val long_q_count: Int = 0,
    val created_at: Long
)

@Entity(
    tableName = "quiz_material_cross_ref",
    primaryKeys = ["quiz_id", "material_id"],
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quiz_id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyMaterialEntity::class,
            parentColumns = ["material_id"],
            childColumns = ["material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quiz_id"]), Index(value = ["material_id"])]
)
data class QuizMaterialCrossRef(
    val quiz_id: Long,
    val material_id: Long
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quiz_id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quiz_id"])]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val question_id: Long = 0,
    val quiz_id: Long,
    val question_type: String, // "MCQ", "SHORT", "LONG"
    val question_text: String,
    val options_json: String?, // JSON string array for MCQs
    val correct_answer: String,
    val explanation: String?
)
```

### Relational Room Queries (With Transaction)

```kotlin
data class QuizWithQuestionsAndMaterials(
    @Embedded val quiz: QuizEntity,
    
    @Relation(
        parentColumn = "quiz_id",
        entityColumn = "question_id"
    )
    val questions: List<QuestionEntity>,

    @Relation(
        parentColumn = "quiz_id",
        entityColumn = "material_id",
        associateBy = Junction(QuizMaterialCrossRef::class)
    )
    val sourceMaterials: List<StudyMaterialEntity>
)
```

---

## 5. Constraints & Data Validation Rules

1. **File Size Enforcement:** The app must validate file size on the client side prior to database insertion (`file_size_mb <= 15.0`).
2. **Cascading Deletes:** Deleting a `Quiz` automatically removes associated records in `quiz_material_cross_ref` and `questions`. Deleting a `StudyMaterial` removes entries in `quiz_material_cross_ref`, while retaining historical generated `Quiz` records.
3. **Valid Question Types:** Enforced via app domain constants or DB check constraints: `MCQ`, `SHORT`, `LONG`.