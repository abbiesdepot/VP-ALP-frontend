package com.abbie.alpvp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.abbie.alpvp.repositories.AuthenticationRepository
import com.abbie.alpvp.repositories.AuthenticationRepositoryInterface
import com.abbie.alpvp.repositories.RewardsRepository
import com.abbie.alpvp.repositories.RewardsRepositoryInterface
import com.abbie.alpvp.repositories.ScheduleRepository
import com.abbie.alpvp.repositories.ScheduleRepositoryInterface
import com.abbie.alpvp.repositories.ScheduleActivityRepository
import com.abbie.alpvp.repositories.ScheduleActivityRepositoryInterface
import com.abbie.alpvp.repositories.UserRepository
import com.abbie.alpvp.repositories.UserRepositoryInterface
import com.abbie.alpvp.services.AuthenticationAPIService
import com.abbie.alpvp.services.ScheduleAPIService
import com.abbie.alpvp.services.ScheduleActivityAPIService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.abbie.alpvp.repositories.TaskRepository
import com.abbie.alpvp.repositories.TaskRepositoryInterface
import com.abbie.alpvp.services.RewardsAPIService
import com.abbie.alpvp.services.TaskApiService

interface AppContainerInterface {
    val authenticationRepository: AuthenticationRepositoryInterface
    val userRepository: UserRepositoryInterface
    val scheduleRepository: ScheduleRepositoryInterface
    val scheduleActivityRepository: ScheduleActivityRepositoryInterface
    val taskRepository: TaskRepositoryInterface
    val rewardsRepository: RewardsRepositoryInterface
}

class AppContainer (
    private val dataStore: DataStore<Preferences>
): AppContainerInterface {
    private val backendURL = "http://10.0.2.2:3000/"
    //private val backendURL = "http://192.168.18.13:6000/"

    private val authenticationRetrofitService: AuthenticationAPIService by lazy {
        val retrofit = initRetrofit()
        retrofit.create(AuthenticationAPIService::class.java)
    }

    private val scheduleAPIService: ScheduleAPIService by lazy {
        val retrofit = initRetrofit()
        retrofit.create(ScheduleAPIService::class.java)
    }

    private val scheduleActivityAPIService: ScheduleActivityAPIService by lazy {
        val retrofit = initRetrofit()
        retrofit.create(ScheduleActivityAPIService::class.java)
    }

    private val taskAPIService: TaskApiService by lazy {
        initRetrofit().create(TaskApiService::class.java)
    }

    private val rewardsAPIService: RewardsAPIService by lazy {
        initRetrofit().create(RewardsAPIService::class.java)
    }


    override val authenticationRepository: AuthenticationRepositoryInterface by lazy {
        AuthenticationRepository(authenticationRetrofitService)
    }

    override val userRepository: UserRepositoryInterface by lazy {
        UserRepository(dataStore)
    }

    override val scheduleRepository: ScheduleRepositoryInterface by lazy {
        ScheduleRepository(scheduleAPIService)
    }

    override val scheduleActivityRepository: ScheduleActivityRepositoryInterface by lazy {
        ScheduleActivityRepository(scheduleActivityAPIService)
    }

    override val taskRepository: TaskRepositoryInterface by lazy {
        TaskRepository(taskAPIService)
    }

    override val rewardsRepository: RewardsRepositoryInterface by lazy {
        RewardsRepository(rewardsAPIService)
    }

    private fun initRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        logging.level = (HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
        client.addInterceptor(logging)

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .baseUrl(backendURL)
            .build()
    }
}