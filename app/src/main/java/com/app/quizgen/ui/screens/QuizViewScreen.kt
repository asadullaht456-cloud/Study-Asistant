package com.app.quizgen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.quizgen.ui.components.DifficultyLevel
import com.app.quizgen.ui.components.DifficultyChip
import com.app.quizgen.ui.components.QuestionCard
import com.app.quizgen.ui.components.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizViewScreen(
    quizId: String,
    onNavigateBack: () -> Unit
) {
    // Mocks for Phase 5
    val title = "Biology Chapter 1"
    val generatedDate = "2026-10-07"
    val difficulty = DifficultyLevel.MEDIUM
    val sourceTags = listOf("Chapter1.pdf", "Notes.docx")
    
    val filters = listOf("All", "MCQs", "Short Qs", "Long Qs")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Viewer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Details
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DifficultyChip(level = difficulty)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Generated: $generatedDate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sources: ${sourceTags.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Chips (Sticky Top representation)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            HorizontalDivider()

            // Questions List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Mock MCQ
                if (selectedFilter == "All" || selectedFilter == "MCQs") {
                    item {
                        QuestionCard(
                            index = 1,
                            type = QuestionType.MCQ,
                            questionText = "Which of the following best describes mitochondria?",
                            options = listOf("Energy production", "Protein synthesis", "Cell division", "Waste removal"),
                            correctOptionIndex = 0,
                            modelAnswer = "Energy production",
                            explanation = "Mitochondria are often referred to as the powerhouse of the cell because they generate most of the cell's supply of ATP."
                        )
                    }
                }
                
                // Mock Short Question
                if (selectedFilter == "All" || selectedFilter == "Short Qs") {
                    item {
                        QuestionCard(
                            index = 2,
                            type = QuestionType.SHORT,
                            questionText = "Define photosynthesis.",
                            modelAnswer = "The process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water.",
                            explanation = "Photosynthesis generally involves the green pigment chlorophyll and generates oxygen as a byproduct."
                        )
                    }
                }

                // Mock Long Question
                if (selectedFilter == "All" || selectedFilter == "Long Qs") {
                    item {
                        QuestionCard(
                            index = 3,
                            type = QuestionType.LONG,
                            questionText = "Explain the detailed phases of the cell cycle.",
                            modelAnswer = "The cell cycle consists of Interphase (G1, S, G2) and the Mitotic phase (M phase).",
                            explanation = "During G1, the cell grows. In S phase, DNA is replicated. In G2, the cell prepares for division. The M phase includes mitosis and cytokinesis, dividing the genetic material and cytoplasm."
                        )
                    }
                }
            }
        }
    }
}
