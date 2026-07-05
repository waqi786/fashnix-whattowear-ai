package com.fashnix.app.ui.planner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.model.Outfit
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.PlannerRepository
import com.fashnix.app.data.repository.WardrobeRepository
import com.fashnix.app.domain.StreakManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val plannerRepository: PlannerRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _outfits = MutableStateFlow<List<Outfit>>(emptyList())
    val outfits = _outfits.asStateFlow()

    private val _wardrobeItems = MutableLiveData<List<ClothingItem>>()
    val wardrobeItems: LiveData<List<ClothingItem>> = _wardrobeItems

    fun loadWardrobe() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            wardrobeRepository.getUserItems(userId).onSuccess {
                _wardrobeItems.postValue(it)
            }
        }
    }

    suspend fun loadOutfitsForDate(date: Long) {
        val userId = authRepository.getCurrentUserId() ?: return
        plannerRepository.getOutfitsForDate(userId, date).fold(
            onSuccess = { _outfits.value = it },
            onFailure = { }
        )
    }

    suspend fun saveOutfit(outfit: Outfit) {
        val userId = authRepository.getCurrentUserId() ?: return
        plannerRepository.saveOutfit(userId, outfit)
    }

    suspend fun deleteOutfit(outfitId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        plannerRepository.deleteOutfit(userId, outfitId).onSuccess {
            _outfits.value = _outfits.value.filter { it.id != outfitId }
        }
    }

    suspend fun markOutfitWorn(outfit: Outfit) {
        val userId = authRepository.getCurrentUserId() ?: return
        plannerRepository.markOutfitWorn(userId, outfit)
        StreakManager.updateStreak(userId, firestore)
        // Increment wearCount for each item in the outfit
        outfit.items.forEach { item ->
            val updatedItem = item.copy(wearCount = item.wearCount + 1, lastWorn = System.currentTimeMillis())
            wardrobeRepository.updateItem(updatedItem)
        }
        _outfits.value = _outfits.value.map { if (it.id == outfit.id) it.copy(isWorn = true) else it }
    }
}