package com.abbie.alpvp.repositories

import com.abbie.alpvp.models.RewardsGalleryResponse
import com.abbie.alpvp.services.RewardsAPIService
import retrofit2.Call

interface RewardsRepositoryInterface {
    fun getRewardsGallery(token: String): Call<RewardsGalleryResponse>
}

class RewardsRepository(
    private val rewardsAPIService: RewardsAPIService
): RewardsRepositoryInterface {

    override fun getRewardsGallery(token: String): Call<RewardsGalleryResponse> {
        return rewardsAPIService.getRewardsGallery("Bearer $token")
    }
}