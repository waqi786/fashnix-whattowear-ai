package com.fashnix.app.ui.wardrobe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.WardrobeRepository
import com.fashnix.app.domain.StreakManager
import com.fashnix.app.utils.SampleData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WardrobeViewModel: The Advanced Engine of the Fashnix Digital Vault.
 * 
 * Engineered with enterprise-grade logic for sorting, filtering, and 
 * asset management. Optimized for high-density performance.
 */
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _items = MutableLiveData<List<ClothingItem>>()
    val items: LiveData<List<ClothingItem>> = _items

    private val _filteredItems = MutableLiveData<List<ClothingItem>>()
    val filteredItems: LiveData<List<ClothingItem>> = _filteredItems

    private val _selectedItem = MutableLiveData<ClothingItem?>()
    val selectedItem: LiveData<ClothingItem?> = _selectedItem

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    private var currentCategory: String? = null
    private var currentSearch: String? = null
    private var currentSortOrder: SortOrder = SortOrder.NEWEST

    enum class SortOrder {
        NEWEST, OLDEST, PRICE_HIGH, PRICE_LOW, MOST_WORN
    }

    fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _errorEvent.value = "Please sign in to view your wardrobe."
                _items.postValue(emptyList())
                _filteredItems.postValue(emptyList())
                _isLoading.value = false
                return@launch
            }
            
            val result = wardrobeRepository.getUserItems(userId)
            if (result.isSuccess) {
                val finalItems = ensureCuratedSeed(userId, result.getOrDefault(emptyList()))
                _items.postValue(finalItems)
                applyAdvancedFilters(finalItems)
            } else {
                _errorEvent.value = "SYNC_ERROR: ${result.exceptionOrNull()?.message}"
                val samples = SampleData.getSampleItems(userId)
                _items.postValue(samples)
                applyAdvancedFilters(samples)
            }
            _isLoading.value = false
        }
    }

    private fun ensureCuratedSeed(userId: String, remoteItems: List<ClothingItem>): List<ClothingItem> {
        val deprecatedSeedIds = setOf("seed_a5", "seed_a6")
        val visibleRemoteItems = remoteItems.filterNot { it.id in deprecatedSeedIds }
        val seedItems = SampleData.getSampleItems(userId)
        val remoteIds = visibleRemoteItems.map { it.id }.toSet()
        val missingSeedItems = seedItems.filterNot { remoteIds.contains(it.id) }

        if (remoteItems.any { it.id in deprecatedSeedIds } || remoteItems.size < seedItems.size || missingSeedItems.isNotEmpty()) {
            viewModelScope.launch {
                deprecatedSeedIds.forEach { id ->
                    firestore.collection("wardrobe").document(id).delete()
                }
                missingSeedItems.forEach { item ->
                    wardrobeRepository.addItem(item)
                }
            }
        }

        return (visibleRemoteItems + missingSeedItems)
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<ClothingItem> { it.id.startsWith("seed_") }
                    .thenBy { it.category }
                    .thenBy { it.name }
            )
    }

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            val localItem = _items.value?.find { it.id == itemId }
            if (localItem != null) {
                _selectedItem.postValue(localItem)
            } else {
                val userId = authRepository.getCurrentUserId()
                val sampleItem = userId?.let { SampleData.getSampleItems(it).find { item -> item.id == itemId } }
                if (sampleItem != null) {
                    _selectedItem.postValue(sampleItem)
                    return@launch
                }

                wardrobeRepository.getItem(itemId).fold(
                    onSuccess = { _selectedItem.postValue(it) },
                    onFailure = {
                        _errorEvent.value = "ITEM_NOT_FOUND: This closet item is no longer available."
                        _selectedItem.postValue(null)
                    }
                )
            }
        }
    }

    /**
     * updateItem: Professional suspend function for asset synchronization.
     * Returns a Result to allow the UI layer to handle success/failure signals.
     */
    suspend fun updateItem(item: ClothingItem): Result<Unit> {
        val result = wardrobeRepository.updateItem(item)
        if (result.isSuccess) {
            loadItems()
            _selectedItem.postValue(item)
        }
        return result
    }

    fun deleteItem(item: ClothingItem) {
        viewModelScope.launch {
            _isLoading.value = true
            wardrobeRepository.deleteItem(item.id).onSuccess {
                loadItems()
            }.onFailure {
                _errorEvent.value = "DELETE_FAILED: Asset protection active."
            }
            _isLoading.value = false
        }
    }

    fun setCategoryFilter(category: String?) {
        currentCategory = if (category == "ALL" || category.isNullOrEmpty()) null else category
        applyAdvancedFilters()
    }

    fun setSearchQuery(query: String?) {
        currentSearch = query
        applyAdvancedFilters()
    }

    fun setSortOrder(order: SortOrder) {
        currentSortOrder = order
        applyAdvancedFilters()
    }

    private fun applyAdvancedFilters(baseList: List<ClothingItem>? = null) {
        var list = baseList ?: _items.value ?: return
        
        if (!currentCategory.isNullOrEmpty()) {
            list = list.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }
        
        if (!currentSearch.isNullOrEmpty()) {
            val query = currentSearch!!.lowercase()
            list = list.filter { 
                it.name.lowercase().contains(query) || 
                it.brand.lowercase().contains(query) ||
                it.color.lowercase().contains(query)
            }
        }
        
        list = when (currentSortOrder) {
            SortOrder.NEWEST -> list.sortedByDescending { it.id }
            SortOrder.OLDEST -> list.sortedBy { it.id }
            SortOrder.PRICE_HIGH -> list.sortedByDescending { it.price }
            SortOrder.PRICE_LOW -> list.sortedBy { it.price }
            SortOrder.MOST_WORN -> list.sortedByDescending { it.wearCount }
        }
        
        _filteredItems.postValue(list)
    }

    fun markAsWorn(item: ClothingItem) {
        val userId = authRepository.getCurrentUserId() ?: return
        val updatedItem = item.copy(
            wearCount = item.wearCount + 1,
            lastWorn = System.currentTimeMillis()
        )
        viewModelScope.launch {
            updateItem(updatedItem).onSuccess {
                StreakManager.updateStreak(userId, firestore)
            }
        }
    }
}
