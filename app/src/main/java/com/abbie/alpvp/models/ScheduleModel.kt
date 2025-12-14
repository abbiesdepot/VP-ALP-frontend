package com.abbie.alpvp.models

import com.google.gson.annotations.SerializedName

data class ScheduleModel(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("userId")
    val userId: Int = 0,
    @SerializedName("date")
    val date: String = "",
    @SerializedName("totalTasks")
    val totalTasks: Int = 0,
    @SerializedName("completedTasks")
    val completedTasks: Int = 0,
    @SerializedName("progressPercentage")
    val progressPercentage: Float = 0f
)

data class GetAllScheduleResponse(
    @SerializedName("data")
    val data: List<ScheduleModel>
)

data class GetScheduleResponse(
    @SerializedName("data")
    val data: ScheduleModel
)

data class ScheduleRequest(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("date")
    val date: String = "",
    @SerializedName("totalTasks")
    val totalTasks: Int? = null,
    @SerializedName("completedTasks")
    val completedTasks: Int? = null,
    @SerializedName("progressPercentage")
    val progressPercentage: Float? = null
)