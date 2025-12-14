package com.abbie.alpvp.services

import com.abbie.alpvp.models.GeneralResponseModel
import com.abbie.alpvp.models.GetAllScheduleResponse
import com.abbie.alpvp.models.GetScheduleResponse
import com.abbie.alpvp.models.ScheduleRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ScheduleAPIService {
    @GET("api/schedule/user/{userId}")
    fun getAllSchedules(@Header("Authorization") token: String, @Path("userId") userId: Int): Call<GetAllScheduleResponse>

    @POST("api/schedule")
    fun createSchedule(@Header("Authorization") token: String, @Body body: ScheduleRequest): Call<GetScheduleResponse>

    @PUT("api/schedule")
    fun updateSchedule(@Header("Authorization") token: String, @Body scheduleModel: ScheduleRequest): Call<GeneralResponseModel>
}