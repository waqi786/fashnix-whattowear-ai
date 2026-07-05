package com.fashnix.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashnix.app.data.local.PreferencesDataStore
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.model.UserProfile
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ProfileUiState: Represents the holistic state of the User Profile screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isPhotoUploading: Boolean = false,
    val userProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val vaultValuation: Double = 0.0,
    val deploymentRate: Int = 0,
    val influencePoints: Int = 2400,
    val apparelPercent: Int = 0,
    val footwearPercent: Int = 0,
    val accessoriesPercent: Int = 0,
    val stylingCount: Int = 0,
    val trophyCount: Int = 0,
    val styleEvolutionScore: Float = 0.0f
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _wardrobeItems = MutableStateFlow<List<ClothingItem>>(emptyList())

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "IDENTITY_NOT_FOUND") }
                return@launch
            }

            val profileResult = authRepository.getUserProfile(userId)
            val wardrobeResult = wardrobeRepository.getUserItems(userId)

            profileResult.fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(userProfile = profile) }
                    
                    wardrobeResult.onSuccess { items ->
                        _wardrobeItems.value = items
                        executeAdvancedAnalytics(profile, items)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun executeAdvancedAnalytics(profile: UserProfile, items: List<ClothingItem>) {
        if (items.isEmpty()) {
            _uiState.update { it.copy(
                vaultValuation = 0.0,
                deploymentRate = 0,
                apparelPercent = 0,
                footwearPercent = 0,
                accessoriesPercent = 0
            ) }
            return
        }

        val totalValuation = items.sumOf { it.price }
        val activeItems = items.count { it.wearCount > 0 }
        val deploymentRate = (activeItems.toDouble() / items.size * 100).toInt()

        val totalSize = items.size.toDouble()
        val apparel = items.count { it.category.contains("Apparel", true) || it.category.contains("Clothing", true) }
        val footwear = items.count { it.category.contains("Footwear", true) || it.category.contains("Shoes", true) }
        val accessories = items.count { it.category.contains("Accessories", true) }

        val averageWear = items.map { it.wearCount }.average()
        val evolutionScore = (averageWear.toFloat() * 0.5f) + (deploymentRate * 0.5f)

        _uiState.update { state ->
            state.copy(
                vaultValuation = totalValuation,
                deploymentRate = deploymentRate,
                apparelPercent = ((apparel / totalSize) * 100).toInt(),
                footwearPercent = ((footwear / totalSize) * 100).toInt(),
                accessoriesPercent = ((accessories / totalSize) * 100).toInt(),
                influencePoints = profile.points.coerceAtLeast(2400),
                stylingCount = (profile.points / 8).coerceAtLeast(145),
                trophyCount = (profile.points / 450).coerceAtLeast(7),
                styleEvolutionScore = evolutionScore
            )
        }
    }

    fun updateStyleDNA(archetype: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(userId).onSuccess { profile ->
                val updatedProfile = profile.copy(styleDNA = archetype)
                authRepository.updateUserProfile(updatedProfile)
                _uiState.update { it.copy(userProfile = updatedProfile) }
            }
        }
    }

    fun updateProfilePhoto(photoUrl: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(userId).onSuccess { profile ->
                val updatedProfile = profile.copy(photoUrl = photoUrl)
                authRepository.updateUserProfile(updatedProfile)
                _uiState.update { it.copy(userProfile = updatedProfile) }
            }
        }
    }

    fun uploadProfilePhoto(imageData: ByteArray) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Sign in again to update your photo") }
                return@launch
            }
            _uiState.update { it.copy(isPhotoUploading = true, errorMessage = null) }
            authRepository.uploadProfilePhoto(userId, imageData).fold(
                onSuccess = { photoUrl ->
                    authRepository.getUserProfile(userId).onSuccess { profile ->
                        val updatedProfile = profile.copy(photoUrl = photoUrl)
                        authRepository.updateUserProfile(updatedProfile)
                        _uiState.update { it.copy(userProfile = updatedProfile) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Profile photo upload failed") }
                }
            )
            _uiState.update { it.copy(isPhotoUploading = false) }
        }
    }

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesDataStore.setLanguage(languageCode)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
