package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.ScheduleActivityModel
import com.abbie.alpvp.models.ScheduleActivityRequest
import com.abbie.alpvp.models.ScheduleModel
import com.abbie.alpvp.models.ScheduleRequest
import com.abbie.alpvp.repositories.ScheduleActivityRepositoryInterface
import com.abbie.alpvp.repositories.ScheduleRepositoryInterface
import com.abbie.alpvp.repositories.UserRepositoryInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardState(
    val username: String = "User",
    val currentScheduleId: Int = 0,
    val todaySchedule: ScheduleModel? = null,
    val activities: List<ScheduleActivityModel> = emptyList(),
    val progress: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val upcomingActivities: List<ScheduleActivityModel> = emptyList(),
    val currentActivity: ScheduleActivityModel? = null,
    val toastMessage: String? = null
)

class DashboardViewModel(
    private val scheduleRepo: ScheduleRepositoryInterface,
    private val activityRepo: ScheduleActivityRepositoryInterface,
    private val userRepo: UserRepositoryInterface
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState = _dashboardState.asStateFlow()

    init {
        startTimerLoop()
    }

    fun clearToastMessage() {
        _dashboardState.update { it.copy(toastMessage = null) }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                val username = userRepo.currentUsername.first()
                val userId = userRepo.currentUserId.first()

                _dashboardState.update { it.copy(username = username) }

                if (userId == -1 || token.isEmpty()) return@launch

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayString = dateFormat.format(Date())

                val isoDateString = "${todayString}T00:00:00.000Z"

                val scheduleRes = scheduleRepo.getAllSchedules(token, userId).awaitResponse()
                val allSchedules = scheduleRes.body()?.data ?: emptyList()

                var currentScheduleObj = allSchedules.find { it.date.startsWith(todayString) }

                if (currentScheduleObj == null) {
                    val request = ScheduleRequest(userId = userId, date = isoDateString)
                    val createRes = scheduleRepo.createSchedule(token, request).awaitResponse()
                    if (createRes.isSuccessful && createRes.body() != null) {
                        currentScheduleObj = createRes.body()!!.data
                    }
                }

                if (currentScheduleObj != null && currentScheduleObj.id != 0) {
                    _dashboardState.update {
                        it.copy(currentScheduleId = currentScheduleObj.id, todaySchedule = currentScheduleObj)
                    }
                    fetchActivities(token, currentScheduleObj.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchActivities(token: String, scheduleId: Int) {
        viewModelScope.launch {
            try {
                val res = activityRepo.getActivitiesBySchedule(token, scheduleId).awaitResponse()
                if (res.isSuccessful) {
                    val activities = res.body()?.data ?: emptyList()
                    _dashboardState.update { it.copy(activities = activities) }
                    calculateProgress()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addActivity(iconName: String, startTime: String, endTime: String, desc: String) {
        viewModelScope.launch {
            try {
                val newStartMinutes = parseIsoToMinutes(startTime)
                val newEndMinutes = parseIsoToMinutes(endTime)
                val currentActivities = _dashboardState.value.activities

                if (newEndMinutes <= newStartMinutes) {
                    _dashboardState.update { it.copy(toastMessage = "End time must be later than start time!") }
                    return@launch
                }

                val isOverlapping = currentActivities.any { existing ->
                    val exStart = parseIsoToMinutes(existing.startTime)
                    val exEnd = parseIsoToMinutes(existing.endTime)
                    newStartMinutes < exEnd && newEndMinutes > exStart
                }

                if (isOverlapping) {
                    _dashboardState.update { it.copy(toastMessage = "Schedule overlaps with existing task!") }
                    return@launch
                }

                val token = userRepo.currentUserToken.first()
                val currentId = _dashboardState.value.currentScheduleId
                if (currentId == 0) return@launch

                val requestModel = ScheduleActivityRequest(
                    scheduleId = currentId,
                    iconName = iconName,
                    startTime = startTime,
                    endTime = endTime,
                    description = desc
                )

                val res = activityRepo.createScheduleActivity(token, requestModel).awaitResponse()
                if (res.isSuccessful) {
                    fetchActivities(token, currentId)
                } else {
                    _dashboardState.update { it.copy(toastMessage = "Failed to add activity") }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                calculateProgress()
                delay(10000)
            }
        }
    }

    private fun calculateProgress() {
        val state = _dashboardState.value
        if (state.currentScheduleId == 0) return

        val activities = state.activities
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeValue = currentHour * 60 + currentMinute

        val doneActivitiesCount = activities.count {
            val endTimeValue = parseIsoToMinutes(it.endTime)
            currentTimeValue >= endTimeValue
        }
        val totalItems = activities.size
        val percentage = if (totalItems > 0) (doneActivitiesCount.toFloat() / totalItems.toFloat()) else 0f

        val currentActivity = activities.find {
            val startVal = parseIsoToMinutes(it.startTime)
            val endVal = parseIsoToMinutes(it.endTime)
            currentTimeValue >= startVal && currentTimeValue < endVal
        }

        val upcomingList = activities
            .filter {
                val startTimeValue = parseIsoToMinutes(it.startTime)
                startTimeValue > currentTimeValue
            }
            .sortedBy { it.startTime }
            .take(3)

        _dashboardState.update {
            it.copy(
                progress = percentage,
                completedCount = doneActivitiesCount,
                totalCount = totalItems,
                upcomingActivities = upcomingList,
                currentActivity = currentActivity
            )
        }

        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                if (token.isNotEmpty()) {
                    val updateReq = ScheduleRequest(
                        id = state.currentScheduleId,
                        totalTasks = totalItems,
                        completedTasks = doneActivitiesCount,
                        progressPercentage = percentage * 100
                    )
                    scheduleRepo.updateSchedule(token, updateReq).awaitResponse()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun parseIsoToMinutes(isoString: String): Int {
        return try {
            if (isoString.contains("T")) {
                val timePart = isoString.split("T")[1].substring(0, 5)
                val parts = timePart.split(":")
                (parts[0].toInt() * 60) + parts[1].toInt()
            } else 0
        } catch (e: Exception) { 0 }
    }
}