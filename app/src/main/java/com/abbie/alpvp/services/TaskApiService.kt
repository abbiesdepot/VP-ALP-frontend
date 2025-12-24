package com.abbie.alpvp.services

import com.abbie.alpvp.models.CreateTask
import com.abbie.alpvp.models.GeneralTaskResponseModel
import com.abbie.alpvp.models.GetAllTasksResponse
import com.abbie.alpvp.models.GetTaskResponse
import com.abbie.alpvp.models.UpdateTask
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskAPIService {

    @GET("tasks/user/{userId}")
    fun getAllTasks(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Call<GetAllTasksResponse>

    @POST("tasks")
    fun createTask(
        @Header("Authorization") token: String,
        @Body request: CreateTask
    ): Call<GetTaskResponse>

    @PUT("tasks")
    fun updateTask(@Header("Authorization") token: String, @Body request: UpdateTask
    ): Call<GetTaskResponse>

    @DELETE("tasks/{id}")
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: Int
    ): Call<GeneralTaskResponseModel>
}