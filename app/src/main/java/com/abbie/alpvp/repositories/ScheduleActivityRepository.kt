package com.abbie.alpvp.repositories

import com.abbie.alpvp.models.GeneralResponseModel
import com.abbie.alpvp.models.GetAllScheduleActivityResponse
import com.abbie.alpvp.models.GetScheduleActivityResponse
import com.abbie.alpvp.models.ScheduleActivityRequest
import com.abbie.alpvp.models.ScheduleActivityUpdateRequest
import com.abbie.alpvp.services.ScheduleActivityAPIService
import retrofit2.Call

interface ScheduleActivityRepositoryInterface {
    fun getActivitiesBySchedule(token: String, scheduleId: Int): Call<GetAllScheduleActivityResponse>

    fun createScheduleActivity(token: String, request: ScheduleActivityRequest): Call<GetScheduleActivityResponse>

    fun updateScheduleActivity(token: String, id: Int, request: ScheduleActivityUpdateRequest): Call<GeneralResponseModel>

    fun deleteScheduleActivity(token: String, activityId: Int): Call<GeneralResponseModel>
}

class ScheduleActivityRepository(
    private val scheduleActivityAPIService: ScheduleActivityAPIService
): ScheduleActivityRepositoryInterface {

    override fun getActivitiesBySchedule(token: String, scheduleId: Int): Call<GetAllScheduleActivityResponse> {
        return scheduleActivityAPIService.getActivitiesBySchedule("Bearer $token", scheduleId)
    }

    override fun createScheduleActivity(token: String, request: ScheduleActivityRequest): Call<GetScheduleActivityResponse> {
        return scheduleActivityAPIService.createScheduleActivity("Bearer $token", request)
    }

    override fun updateScheduleActivity(token: String, id: Int, request: ScheduleActivityUpdateRequest): Call<GeneralResponseModel> {
        return scheduleActivityAPIService.updateScheduleActivity("Bearer $token", id, request) }

    override fun deleteScheduleActivity(token: String, activityId: Int): Call<GeneralResponseModel> {
        return scheduleActivityAPIService.deleteScheduleActivity("Bearer $token", activityId)
    }
}