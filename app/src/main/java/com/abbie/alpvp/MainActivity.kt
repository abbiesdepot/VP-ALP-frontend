package com.abbie.alpvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abbie.alpvp.routes.Screen
import com.abbie.alpvp.views.ActivityListScreen
import com.abbie.alpvp.views.DashboardScreen
import com.abbie.alpvp.views.LoginScreen
import com.abbie.alpvp.views.RegisterScreen
import com.abbie.alpvp.views.RewardScreen
import com.abbie.alpvp.views.TaskListScreen
import com.abbie.alpvp.views.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DailyStepApp()
                }
            }
        }
    }
}

@Composable
fun DailyStepApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToActivityList = { navController.navigate(Screen.ManageActivity.route) },
                onNavigateToTaskList = { navController.navigate(Screen.TaskList.route) },
                onNavigateToTimer = { navController.navigate(Screen.Timer.route) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
            )
        }

        composable(Screen.ManageActivity.route) {
            ActivityListScreen(
                onNavigateToDashboard = { navController.popBackStack() }
            )
        }

        composable(Screen.TaskList.route) {
            TaskListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Timer.route) {
            TimerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Rewards.route) {
            RewardScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}