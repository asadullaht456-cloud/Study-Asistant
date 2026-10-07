package com.app.quizgen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.quizgen.ui.components.DifficultyLevel
import com.app.quizgen.ui.components.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuizView: (String) -> Unit
) {
    // Mocks for Phase 4
    val selectedMaterials = remember { mutableStateListOf("Chapter1.pdf", "Notes.docx") }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.MEDIUM) }
    var mcqCount by remember { mutableStateOf(10) }
    var shortCount by remember { mutableStateOf(3) }
    var longCount by remember { mutableStateOf(1) }
    var isGenerating by remember { mutableStateOf(false) }

    val totalQuestions = mcqCount + shortCount + longCount
    val isGenerateEnabled = totalQuestions > 0 && selectedMaterials.isNotEmpty()

    LoadingOverlay(isShowing = isGenerating)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Quiz") },
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
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = { 
                            // Mock generation
                            // isGenerating = true 
                            onNavigateToQuizView("new_quiz_id") 
                        },
                        enabled = isGenerateEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Generate Quiz with AI")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selected Materials Summary
            Text("Selected Materials", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedMaterials) { material ->
                    InputChip(
                        selected = true,
                        onClick = { selectedMaterials.remove(material) },
                        label = { Text(material) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Difficulty Selector
            Text("Difficulty Level", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DifficultyLevel.values().forEach { level ->
                    FilterChip(
                        selected = selectedDifficulty == level,
                        onClick = { selectedDifficulty = level },
                        label = { Text(level.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Question Counter Matrix
            Text("Question Counts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            QuestionCounterRow(label = "Multiple Choice (MCQs)", count = mcqCount) { mcqCount = it }
            QuestionCounterRow(label = "Short Answer Questions", count = shortCount) { shortCount = it }
            QuestionCounterRow(label = "Long Answer Questions", count = longCount) { longCount = it }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Total Summary Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Questions:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$totalQuestions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCounterRow(label: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("-")
            }
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OutlinedButton(
                onClick = { onCountChange(count + 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("+")
            }
        }
    }
}
