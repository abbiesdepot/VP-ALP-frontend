package com.abbie.alpvp.repositories

import com.abbie.alpvp.models.GeneralResponseModel
import com.abbie.alpvp.models.GetAllScheduleResponse
import com.abbie.alpvp.models.GetScheduleResponse
import com.abbie.alpvp.models.ScheduleRequest
import com.abbie.alpvp.services.ScheduleAPIService
import retrofit2.Call

interface ScheduleRepositoryInterface {
    fun getAllSchedules(token: String, userId: Int): Call<GetAllScheduleResponse>

    fun createSchedule(token: String, body: ScheduleRequest): Call<GetScheduleResponse>

    fun updateSchedule(token: String, request: ScheduleRequest): Call<GeneralResponseModel>
}

class ScheduleRepository(
    private val scheduleAPIService: ScheduleAPIService
): ScheduleRepositoryInterface {
    override fun getAllSchedules(token: String, userId: Int): Call<GetAllScheduleResponse> {
        return scheduleAPIService.getAllSchedules("Bearer $token", userId)
    }

    override fun createSchedule(token: String, body: ScheduleRequest): Call<GetScheduleResponse> {
        return scheduleAPIService.createSchedule("Bearer $token", body)
    }

    override fun updateSchedule(token: String, request: ScheduleRequest): Call<GeneralResponseModel> {
        return scheduleAPIService.updateSchedule("Bearer $token", request)
    }
}