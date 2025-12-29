package com.abbie.alpvp.services

import com.abbie.alpvp.models.RewardsGalleryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface RewardsAPIService {

    @GET("rewards/gallery")
    fun getRewardsGallery(
        @Header("Authorization") token: String
    ): Call<RewardsGalleryResponse>
}