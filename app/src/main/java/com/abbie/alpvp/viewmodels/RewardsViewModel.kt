package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbie.alpvp.models.RewardModel
import com.abbie.alpvp.repositories.RewardsRepositoryInterface
import com.abbie.alpvp.repositories.UserRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class RewardsUiState(
    val earnedRewards: List<RewardModel> = emptyList(),
    val lockedRewards: List<RewardModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RewardsViewModel(
    private val rewardsRepository: RewardsRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : ViewModel() {
    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    fun loadRewards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val token = try {
                userRepository.currentUserToken.first()
            } catch (e: Exception) {
                ""
            }

            if (token.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Not authenticated")
                return@launch
            }

            rewardsRepository.getRewardsGallery(token).enqueue(object : Callback<com.abbie.alpvp.models.RewardsGalleryResponse> {
                override fun onResponse(
                    call: Call<com.abbie.alpvp.models.RewardsGalleryResponse>,
                    response: Response<com.abbie.alpvp.models.RewardsGalleryResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!.data
                        val sortedEarned = data.earnedRewards.sortedBy { it.threshold }
                        val sortedLocked = data.lockedRewards.sortedBy { it.threshold }

                        _uiState.value = RewardsUiState(
                            earnedRewards = sortedEarned,
                            lockedRewards = sortedLocked,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load rewards: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<com.abbie.alpvp.models.RewardsGalleryResponse>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = t.message ?: "Failed to load rewards"
                    )
                }
            })
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}