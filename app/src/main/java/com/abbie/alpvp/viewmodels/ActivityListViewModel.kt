package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.ScheduleActivityModel
import com.abbie.alpvp.models.ScheduleActivityUpdateRequest
import com.abbie.alpvp.repositories.ScheduleActivityRepositoryInterface
import com.abbie.alpvp.repositories.ScheduleRepositoryInterface
import com.abbie.alpvp.repositories.UserRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityListViewModel(
    private val activityRepo: ScheduleActivityRepositoryInterface,
    private val scheduleRepo: ScheduleRepositoryInterface,
    private val userRepo: UserRepositoryInterface
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<ScheduleActivityModel>>(emptyList())
    val uiState = _uiState.asStateFlow()

    fun loadActivities() {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                val userId = userRepo.currentUserId.first()

                if (userId == -1 || token.isEmpty()) return@launch

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayString = dateFormat.format(Date())

                val schedules = scheduleRepo.getAllSchedules(token, userId).awaitResponse()
                val allSchedules = schedules.body()?.data ?: emptyList()

                val todaySchedule = allSchedules.find {
                    it.date.startsWith(todayString)
                }

                if (todaySchedule != null) {
                    val actRes = activityRepo.getActivitiesBySchedule(token, todaySchedule.id).awaitResponse()
                    if (actRes.isSuccessful) {
                        _uiState.value = actRes.body()?.data ?: emptyList()
                    }
                } else {
                    _uiState.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateActivity(id: Int, iconName: String, startTime: String, endTime: String, description: String) {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                if (token.isEmpty()) return@launch

                val request = ScheduleActivityUpdateRequest(
                    id = id,
                    iconName = iconName,
                    startTime = startTime,
                    endTime = endTime,
                    description = description
                )

                val res = activityRepo.updateScheduleActivity(token, id, request).awaitResponse()

                if (res.isSuccessful) {
                    android.util.Log.d("ActivityListVM", "Update Sukses")
                    loadActivities()
                } else {
                    android.util.Log.e("ActivityListVM", "Gagal Update: ${res.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteActivity(activityId: Int) {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                val res = activityRepo.deleteScheduleActivity(token, activityId).awaitResponse()
                if (res.isSuccessful) {
                    loadActivities()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}