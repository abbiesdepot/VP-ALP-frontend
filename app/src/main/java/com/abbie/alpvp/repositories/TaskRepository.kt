package com.abbie.alpvp.repositories

import com.abbie.alpvp.models.CreateTask
import com.abbie.alpvp.models.GeneralTaskResponseModel
import com.abbie.alpvp.models.GetAllTasksResponse
import com.abbie.alpvp.models.GetTaskResponse
import com.abbie.alpvp.models.UpdateTask
import com.abbie.alpvp.services.TaskAPIService
import retrofit2.Call


interface TaskRepositoryInterface {
    fun getAllTasks(token: String, userId: Int): Call<GetAllTasksResponse>

    fun createTask(token: String, request: CreateTask): Call<GetTaskResponse>

    fun updateTask(token: String, request: UpdateTask): Call<GetTaskResponse>

    fun deleteTask(token: String, taskId: Int): Call<GeneralTaskResponseModel>
}

class TaskRepository(
    private val taskAPIService: TaskAPIService
): TaskRepositoryInterface {

    override fun getAllTasks(token: String, userId: Int): Call<GetAllTasksResponse> {
        return taskAPIService.getAllTasks("Bearer $token", userId)
    }

    override fun createTask(token: String, request: CreateTask): Call<GetTaskResponse> {
        return taskAPIService.createTask("Bearer $token", request)
    }

    override fun updateTask(token: String, request: UpdateTask): Call<GetTaskResponse> {
        return taskAPIService.updateTask("Bearer $token", request)
    }

    override fun deleteTask(token: String, taskId: Int): Call<GeneralTaskResponseModel> {
        return taskAPIService.deleteTask("Bearer $token", taskId)
    }
}