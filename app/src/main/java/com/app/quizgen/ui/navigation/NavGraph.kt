package com.app.quizgen.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Upload : Screen("upload")
    object Config : Screen("config")
    object QuizView : Screen("quiz_view")
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            com.app.quizgen.ui.screens.DashboardScreen(
                onNavigateToUpload = { navController.navigate(Screen.Upload.route) },
                onNavigateToQuizView = { quizId -> navController.navigate(Screen.QuizView.route) }
            )
        }
        composable(Screen.Upload.route) {
            com.app.quizgen.ui.screens.UploadScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConfig = { navController.navigate(Screen.Config.route) }
            )
        }
        composable(Screen.Config.route) {
            Text(text = "Config Screen Placeholder")
        }
        composable(Screen.QuizView.route) {
            Text(text = "Quiz View Screen Placeholder")
        }
    }
}
