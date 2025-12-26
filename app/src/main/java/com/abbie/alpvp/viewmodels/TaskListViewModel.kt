package com.abbie.alpvp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.TaskModel
import com.abbie.alpvp.models.UpdateTask
import com.abbie.alpvp.repositories.TaskRepositoryInterface
import com.abbie.alpvp.repositories.UserRepositoryInterface
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class TaskListViewModel(
    private val taskRepo: TaskRepositoryInterface,
    private val userRepo: UserRepositoryInterface
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val todoTasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val finishedTasks = MutableStateFlow<List<TaskModel>>(emptyList())

    fun fetchTasks() = fetchTasks(null)
    fun fetchTasks(scheduleId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userRepo.currentUserToken.first()
                val userId = userRepo.currentUserId.first()

                if (token.isEmpty()) {
                    _errorMessage.value = "User not authenticated"
                    return@launch
                }

                Log.d("TASK_FETCH", "fetchTasks scheduleId=$scheduleId tokenPresent=${token.isNotEmpty()}")

                val response = taskRepo
                    .getAllTasks(token, userId, scheduleId)
                    .awaitResponse()

                Log.d("TASK_FETCH", "getAllTasks response code=${response.code()} isSuccessful=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    Log.d("TASK_FETCH", "response body JSON = ${Gson().toJson(response.body())}")
                    _tasks.value = data

                    todoTasks.value = data.filter { !it.isCompleted }
                    finishedTasks.value = data.filter { it.isCompleted }
                } else {
                    val err = response.errorBody()?.string()
                    _errorMessage.value = "Failed to load tasks: ${response.code()} ${err ?: ""}"
                    Log.d("TASK_FETCH", "getAllTasks failed code=${response.code()} body=$err")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun toIsoInstantUtc(input: String): String? {
        // instant
        try {
            val inst = Instant.parse(input)
            return DateTimeFormatter.ISO_INSTANT.format(inst)
        } catch (_: DateTimeParseException) { }

        // local date time
        try {
            val ldt = LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME)
            return ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        } catch (_: DateTimeParseException) { }

        // dd-mm-yyyy
        try {
            val d = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val start = d.atStartOfDay(ZoneOffset.UTC)
            return DateTimeFormatter.ISO_INSTANT.format(start)
        } catch (_: DateTimeParseException) { }

        // dibalik
        try {
            val d = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE)
            val start = d.atStartOfDay(ZoneOffset.UTC)
            return DateTimeFormatter.ISO_INSTANT.format(start)
        } catch (_: DateTimeParseException) { }

        // ada jam
        try {
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val ldt = LocalDateTime.parse(input, fmt)
            return ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        } catch (_: DateTimeParseException) { }

        return null
    }

    fun createTask(
        scheduleId: Int?,
        title: String,
        deadline: String,
        description: String?
    ) {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                if (token.isEmpty()) {
                    _errorMessage.value = "Not authenticated"
                    return@launch
                }

                // Convert client deadline to ISO instant (UTC)
                val deadlineIso = toIsoInstantUtc(deadline)
                if (deadlineIso == null) {
                    _errorMessage.value = "Invalid deadline format. Use ISO (e.g. 2025-12-26T10:00:00Z) or dd-MM-yyyy"
                    Log.d("TASK_CREATE", "Invalid deadline format input=\"$deadline\"")
                    return@launch
                }

                val request = com.abbie.alpvp.models.CreateTask(
                    scheduleId = scheduleId,
                    title = title,
                    deadline = deadlineIso,
                    description = description
                )

                // Log the exact JSON being sent
                val gson = Gson()
                val json = gson.toJson(request)
                Log.d("TASK_CREATE", "request JSON = $json")

                val response = taskRepo.createTask(token, request).awaitResponse()

                Log.d("TASK_CREATE", "response code=${response.code()} isSuccessful=${response.isSuccessful}")
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                Log.d("TASK_CREATE", "response errorBody=$err")

                if (response.isSuccessful) {
                    fetchTasks()
                } else {
                    _errorMessage.value = "Failed to create task: ${response.code()} ${err ?: response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                if (token.isEmpty()) return@launch

                val request = UpdateTask(
                    id = taskId,
                    isCompleted = isCompleted
                )

                val response = taskRepo
                    .updateTask(token, request)
                    .awaitResponse()

                if (response.isSuccessful) {
                    fetchTasks()
                } else {
                    _errorMessage.value = "Failed to update task"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                val token = userRepo.currentUserToken.first()
                if (token.isEmpty()) return@launch

                val response = taskRepo
                    .deleteTask(token, taskId)
                    .awaitResponse()

                if (response.isSuccessful) {
                    fetchTasks()
                } else {
                    _errorMessage.value = "Failed to delete task"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                e.printStackTrace()
            }
        }
    }

}