package com.app.quizgen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.quizgen.ui.theme.DifficultyEasy
import com.app.quizgen.ui.theme.DifficultyMedium
import com.app.quizgen.ui.theme.DifficultyHard

enum class DifficultyLevel { EASY, MEDIUM, HARD }

@Composable
fun DifficultyChip(level: DifficultyLevel) {
    val backgroundColor = when(level) {
        DifficultyLevel.EASY -> DifficultyEasy
        DifficultyLevel.MEDIUM -> DifficultyMedium
        DifficultyLevel.HARD -> DifficultyHard
    }
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = level.name,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
