package com.abbie.alpvp.routes

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object ManageActivity : Screen("manage_activity")
    object TaskList : Screen("task_list")
    object Timer : Screen("timer")
    object Rewards : Screen("rewards")
}