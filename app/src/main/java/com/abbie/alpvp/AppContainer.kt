package com.abbie.alpvp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.abbie.alpvp.repositories.AuthenticationRepository
import com.abbie.alpvp.repositories.AuthenticationRepositoryInterface
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

interface AppContainerInterface {
    val authenticationRepository: AuthenticationRepositoryInterface
    val userRepository: UserRepositoryInterface
    val scheduleRepository: ScheduleRepositoryInterface
    val scheduleActivityRepository: ScheduleActivityRepositoryInterface
}

class AppContainer (
    private val dataStore: DataStore<Preferences>
): AppContainerInterface {
    private val backendURL = "http://192.168.18.13:6000/"

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