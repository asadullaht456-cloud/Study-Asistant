package com.app.quizgen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.app.quizgen.ui.components.DifficultyLevel
import com.app.quizgen.ui.components.EmptyState
import com.app.quizgen.ui.components.MaterialCard
import com.app.quizgen.ui.components.QuizCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToUpload: () -> Unit,
    onNavigateToQuizView: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Generated Quizzes", "Saved Materials")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Quiz Generator") },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Local Storage Indicator"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToUpload,
                icon = { Icon(Icons.Default.Add, contentDescription = "Create New Quiz") },
                text = { Text("Create New Quiz") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Mocks for now until ViewModel binding in Phase 6
            val isQuizzesEmpty = false
            val isMaterialsEmpty = false

            if (selectedTabIndex == 0) {
                if (isQuizzesEmpty) {
                    EmptyState(
                        message = "No quizzes generated yet. Upload a document to get started.",
                        actionText = "Upload Document",
                        onActionClick = onNavigateToUpload
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(3) {
                            QuizCard(
                                title = "Biology Chapter $it",
                                difficulty = DifficultyLevel.MEDIUM,
                                questionBreakdown = "10 MCQs, 3 Short, 1 Long",
                                dateCreated = "2026-10-0$it",
                                onViewExamClick = { onNavigateToQuizView("quiz_$it") }
                            )
                        }
                    }
                }
            } else {
                if (isMaterialsEmpty) {
                    EmptyState(
                        message = "No materials saved yet. Upload a document to get started.",
                        actionText = "Upload Document",
                        onActionClick = onNavigateToUpload
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(3) {
                            MaterialCard(
                                fileName = "Chapter_$it_Notes.pdf",
                                sizeInMb = 2.5 + it,
                                uploadDate = "2026-10-0$it"
                            )
                        }
                    }
                }
            }
        }
    }
}
