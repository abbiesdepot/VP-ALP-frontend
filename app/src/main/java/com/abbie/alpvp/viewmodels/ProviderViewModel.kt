package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.abbie.alpvp.DailyStepApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                dailyStepApplication().container.authenticationRepository,
                dailyStepApplication().container.userRepository
            )
        }
        initializer {
            DashboardViewModel(
                dailyStepApplication().container.scheduleRepository,
                dailyStepApplication().container.scheduleActivityRepository,
                dailyStepApplication().container.userRepository
            )
        }
        initializer {
            ActivityListViewModel(
                dailyStepApplication().container.scheduleActivityRepository,
                dailyStepApplication().container.scheduleRepository,
                dailyStepApplication().container.userRepository
            )
        }
    }
}

fun CreationExtras.dailyStepApplication(): DailyStepApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DailyStepApplication)