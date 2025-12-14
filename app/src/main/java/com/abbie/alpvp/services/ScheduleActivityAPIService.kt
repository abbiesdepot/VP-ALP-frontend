package com.abbie.alpvp.services

import com.abbie.alpvp.models.GeneralResponseModel
import com.abbie.alpvp.models.GetAllScheduleActivityResponse
import com.abbie.alpvp.models.GetScheduleActivityResponse
import com.abbie.alpvp.models.ScheduleActivityRequest
import com.abbie.alpvp.models.ScheduleActivityUpdateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ScheduleActivityAPIService {
    @GET("api/schedule-activity/schedule/{scheduleId}")
    fun getActivitiesBySchedule(@Header("Authorization") token: String, @Path("scheduleId") scheduleId: Int): Call<GetAllScheduleActivityResponse>

    @POST("api/schedule-activity")
    fun createScheduleActivity(@Header("Authorization") token: String, @Body body: ScheduleActivityRequest): Call<GetScheduleActivityResponse>

    @PUT("api/schedule-activity/{id}")
    fun updateScheduleActivity(@Header("Authorization") token: String, @Path("id") activityId: Int, @Body request: ScheduleActivityUpdateRequest): Call<GeneralResponseModel>

    @DELETE("api/schedule-activity/{id}")
    fun deleteScheduleActivity(@Header("Authorization") token: String, @Path("id") activityId: Int): Call<GeneralResponseModel>
}