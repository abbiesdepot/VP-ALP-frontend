package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.CreateTask
import com.abbie.alpvp.models.TaskModel
import com.abbie.alpvp.models.UpdateTask
import com.abbie.alpvp.repositories.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class TaskUiState(
    val upcomingTasks: List<TaskModel> = emptyList(),
    val completedTasks: List<TaskModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskListViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun loadTasks(userId: Int, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            taskRepository.getAllTasks(token, userId).enqueue(object : Callback<com.abbie.alpvp.models.GetAllTasksResponse> {
                override fun onResponse(
                    call: Call<com.abbie.alpvp.models.GetAllTasksResponse>,
                    response: Response<com.abbie.alpvp.models.GetAllTasksResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val tasks = response.body()!!.data
                        val upcoming = tasks.filter { !it.isCompleted }
                        val completed = tasks.filter { it.isCompleted }
                        _uiState.value = TaskUiState(
                            upcomingTasks = upcoming,
                            completedTasks = completed,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load tasks: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<com.abbie.alpvp.models.GetAllTasksResponse>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = t.message ?: "Failed to load tasks"
                    )
                }
            })
        }
    }

    fun createTask(
        scheduleId: Int?,
        title: String,
        deadline: String,
        description: String?,
        token: String,
        userId: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = CreateTask(
                scheduleId = scheduleId,
                title = title,
                deadline = deadline,
                description = description
            )

            taskRepository.createTask(token, request).enqueue(object : Callback<com.abbie.alpvp.models.GetTaskResponse> {
                override fun onResponse(
                    call: Call<com.abbie.alpvp.models.GetTaskResponse>,
                    response: Response<com.abbie.alpvp.models.GetTaskResponse>
                ) {
                    if (response.isSuccessful) {
                        loadTasks(userId, token)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to create task: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<com.abbie.alpvp.models.GetTaskResponse>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = t.message ?: "Failed to create task"
                    )
                }
            })
        }
    }

    fun toggleTaskCompletion(task: TaskModel, token: String, userId: Int) {
        viewModelScope.launch {
            val request = UpdateTask(
                id = task.id,
                isCompleted = !task.isCompleted
            )

            taskRepository.updateTask(token, request).enqueue(object : Callback<com.abbie.alpvp.models.GetTaskResponse> {
                override fun onResponse(
                    call: Call<com.abbie.alpvp.models.GetTaskResponse>,
                    response: Response<com.abbie.alpvp.models.GetTaskResponse>
                ) {
                    if (response.isSuccessful) {
                        loadTasks(userId, token)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to update task: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<com.abbie.alpvp.models.GetTaskResponse>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        error = t.message ?: "Failed to update task"
                    )
                }
            })
        }
    }

    fun deleteTask(taskId: Int, token: String, userId: Int) {
        viewModelScope.launch {
            taskRepository.deleteTask(token, taskId).enqueue(object : Callback<com.abbie.alpvp.models.GeneralTaskResponseModel> {
                override fun onResponse(
                    call: Call<com.abbie.alpvp.models.GeneralTaskResponseModel>,
                    response: Response<com.abbie.alpvp.models.GeneralTaskResponseModel>
                ) {
                    if (response.isSuccessful) {
                        loadTasks(userId, token)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete task: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<com.abbie.alpvp.models.GeneralTaskResponseModel>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        error = t.message ?: "Failed to delete task"
                    )
                }
            })
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}