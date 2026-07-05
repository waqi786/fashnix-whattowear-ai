package com.fashnix.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashnix.app.data.model.*
import com.fashnix.app.data.repository.*
import com.fashnix.app.utils.SampleData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val scanRepository: ScanRepository,
    private val weatherRepository: WeatherRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _wardrobeItems = MutableLiveData<List<ClothingItem>>()
    val wardrobeItems: LiveData<List<ClothingItem>> = _wardrobeItems

    private val _weatherData = MutableLiveData<WeatherRepository.WeatherData>()
    val weatherData: LiveData<WeatherRepository.WeatherData> = _weatherData

    private val _weatherError = MutableLiveData<String>()
    val weatherError: LiveData<String> = _weatherError

    private val _vaultValue = MutableStateFlow(0.0)
    val vaultValue = _vaultValue.asStateFlow()

    private val _communityPosts = MutableLiveData<List<CommunityPost>>()
    val communityPosts: LiveData<List<CommunityPost>> = _communityPosts

    private val _dailyRecommendations = MutableLiveData<List<ClothingItem>>()
    val dailyRecommendations: LiveData<List<ClothingItem>> = _dailyRecommendations

    private val _upcomingEvents = MutableLiveData<List<StyleEvent>>()
    val upcomingEvents: LiveData<List<StyleEvent>> = _upcomingEvents

    private val _recentActivity = MutableLiveData<List<UserActivity>>()
    val recentActivity: LiveData<List<UserActivity>> = _recentActivity

    private val _sustainabilityMetrics = MutableLiveData<Map<String, Double>>()
    val sustainabilityMetrics: LiveData<Map<String, Double>> = _sustainabilityMetrics

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            loadUserProfile()
            loadWardrobe()
            loadTrends()
            loadMockActivities()
            loadMockEvents()
            loadSustainabilityMetrics()
        }
    }

    private suspend fun loadUserProfile() {
        withContext(Dispatchers.IO) {
            val userId = authRepository.getCurrentUserId() ?: return@withContext
            authRepository.getUserProfile(userId).onSuccess { _userProfile.postValue(it) }
        }
    }

    fun loadWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _wardrobeItems.postValue(emptyList())
                _vaultValue.value = 0.0
                return@launch
            }
            val result = wardrobeRepository.getUserItems(userId)
            if (result.isSuccess) {
                val items = result.getOrDefault(emptyList())
                val list = ensureCuratedSeed(userId, items)
                _wardrobeItems.postValue(list)
                _vaultValue.value = list.sumOf { it.price }
            } else {
                val fallback = SampleData.getSampleItems(userId)
                _wardrobeItems.postValue(fallback)
                _vaultValue.value = fallback.sumOf { it.price }
            }
        }
    }

    private fun ensureCuratedSeed(userId: String, remoteItems: List<ClothingItem>): List<ClothingItem> {
        val deprecatedSeedIds = setOf("seed_a5", "seed_a6")
        val visibleRemoteItems = remoteItems.filterNot { it.id in deprecatedSeedIds }
        val seedItems = SampleData.getSampleItems(userId)
        val remoteIds = visibleRemoteItems.map { it.id }.toSet()
        val missingSeedItems = seedItems.filterNot { remoteIds.contains(it.id) }

        if (remoteItems.any { it.id in deprecatedSeedIds } || remoteItems.size < seedItems.size || missingSeedItems.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
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

    fun loadWeather(lat: Double, lon: Double, city: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getWeather(lat, lon, city)
                .onSuccess { _weatherData.postValue(it) }
                .onFailure { _weatherError.postValue(it.message ?: "Weather not found.") }
        }
    }

    private fun loadTrends() {
        _communityPosts.postValue(listOf(
            CommunityPost("c1", "Classic Black", "StyleBot", "", 1200),
            CommunityPost("c2", "Orange Vibes", "TrendSetter", "", 850)
        ))
    }

    fun generateDailyOutfit() {
        viewModelScope.launch(Dispatchers.Default) {
            val items = _wardrobeItems.value.orEmpty()
            if (items.isNotEmpty()) {
                _dailyRecommendations.postValue(items.shuffled().take(3))
            }
        }
    }

    private fun loadMockActivities() {
        _recentActivity.postValue(listOf(
            UserActivity("1", "Outfit Scanned", "You added a new leather jacket.", "INGESTION"),
            UserActivity("2", "Style Quiz", "Identity calibration complete.", "ACHIEVEMENT"),
            UserActivity("3", "Wardrobe Sync", "Cloud assets synchronized.", "DEPLOYMENT")
        ))
    }

    private fun loadMockEvents() {
        _upcomingEvents.postValue(listOf(
            StyleEvent("1", "Business Meeting", "Formal", System.currentTimeMillis() + 86400000, "City Center"),
            StyleEvent("2", "Weekend Gala", "Black Tie", System.currentTimeMillis() + 259200000, "Grand Hotel")
        ))
    }

    private fun loadSustainabilityMetrics() {
        _sustainabilityMetrics.postValue(mapOf(
            "cpw" to 12.5,
            "avg_utilization" to 65.0
        ))
    }
}
