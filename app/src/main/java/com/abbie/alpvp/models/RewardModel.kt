package com.abbie.alpvp.models

import com.google.gson.annotations.SerializedName

data class RewardModel(
    val id: Int,
    val title: String,
    val description: String? = null,
    @SerializedName("asset_url")
    val assetUrl: String? = null,
    @SerializedName("trigger_type")
    val triggerType: String, // "STREAK", "TASK_COUNT", "DAILY_COMPLETION" di backenddd
    val threshold: Int
)

data class RewardsGalleryResponse(
    val data: RewardsGalleryData
)

data class RewardsGalleryData(
    val earnedRewards: List<RewardModel>,
    val lockedRewards: List<RewardModel>
)