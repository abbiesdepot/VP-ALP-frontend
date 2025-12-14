package com.abbie.alpvp.models

import com.google.gson.annotations.SerializedName

data class ScheduleActivityModel(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("scheduleId")
    val scheduleId: Int = 0,
    @SerializedName("iconName")
    val iconName: String = "",
    @SerializedName("startTime")
    val startTime: String = "",
    @SerializedName("endTime")
    val endTime: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false
)

data class GetAllScheduleActivityResponse(
    @SerializedName("data")
    val data: List<ScheduleActivityModel>
)

data class GetScheduleActivityResponse(
    @SerializedName("data")
    val data: ScheduleActivityModel
)

data class ScheduleActivityRequest(
    @SerializedName("scheduleId")
    val scheduleId: Int = 0,
    @SerializedName("iconName")
    val iconName: String = "",
    @SerializedName("startTime")
    val startTime: String = "",
    @SerializedName("endTime")
    val endTime: String = "",
    @SerializedName("description")
    val description: String = ""
)

data class ScheduleActivityUpdateRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("iconName")
    val iconName: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("isCompleted")
    val isCompleted: Boolean? = null
)