package com.abbie.alpvp.models

import com.google.gson.annotations.SerializedName

data class TaskModel(
    val id: Int,
    val title: String,
    val description: String? = null,
    val deadline: String, // Format: YYYY-MM-DD or DD-MM-YYYY from backend
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("schedule_id")
    val scheduleId: Int? = null
)

data class CreateTask(
    @SerializedName("schedule_id")
    val scheduleId: Int?,
    val title: String,
    val deadline: String,
    val description: String? = null
)

data class UpdateTask(
    val id: Int,
    @SerializedName("is_completed")
    val isCompleted: Boolean? = null,
    val title: String? = null
)

data class GetAllTasksResponse(
    val success: Boolean,
    val message: String,
    val data: List<TaskModel>
)

data class GetTaskResponse(
    val success: Boolean,
    val message: String,
    val data: TaskModel
)

data class GeneralTaskResponseModel(
    val success: Boolean,
    val message: String
)