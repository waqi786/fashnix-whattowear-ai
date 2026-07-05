package com.fashnix.app.ui.scan

import androidx.lifecycle.ViewModel
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.model.ScanResult
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.ScanRepository
import com.fashnix.app.data.repository.WardrobeRepository
import com.fashnix.app.domain.ClassificationResult
import com.fashnix.app.domain.FashnixClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult = _classificationResult.asStateFlow()

    private val _isClassifying = MutableStateFlow(false)
    val isClassifying = _isClassifying.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _wardrobeItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val wardrobeItems = _wardrobeItems.asStateFlow()

    suspend fun classifyImage(classifier: FashnixClassifier, bitmap: android.graphics.Bitmap) {
        _isClassifying.value = true
        try {
            val result = classifier.classify(bitmap)
            _classificationResult.value = result
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isClassifying.value = false
        }
    }

    suspend fun loadWardrobeItems() {
        val userId = authRepository.getCurrentUserId() ?: return
        wardrobeRepository.getUserItems(userId).onSuccess {
            _wardrobeItems.value = it
        }
    }

    suspend fun saveScanAndItem(
        result: ClassificationResult,
        imageUri: String,
        userSelectedColour: String?,
        occasionOverride: String?
    ): Result<Unit> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))

        val scan = ScanResult(
            category = result.category,
            colour = userSelectedColour ?: result.colour,
            occasion = occasionOverride ?: result.occasion,
            gender = result.gender,
            categoryConfidence = result.categoryConfidence,
            colourConfidence = result.colourConfidence,
            occasionConfidence = result.occasionConfidence,
            genderConfidence = result.genderConfidence,
            isColourOther = result.isColourOther,
            userSelectedColour = userSelectedColour,
            imageUrl = imageUri
        )

        val scanSaved = scanRepository.saveScan(userId, scan)
        if (scanSaved.isFailure) return scanSaved.map { }

        val item = ClothingItem(
            userId = userId,
            imageUrl = imageUri,
            category = result.category,
            color = userSelectedColour ?: result.colour,
            occasion = occasionOverride ?: result.occasion,
            gender = result.gender,
            name = buildItemName(result, userSelectedColour, occasionOverride)
        )

        return wardrobeRepository.addItem(item).map { }
    }

    private fun buildItemName(
        result: ClassificationResult,
        userSelectedColour: String?,
        occasionOverride: String?
    ): String {
        val color = userSelectedColour ?: result.colour
        val occasion = occasionOverride ?: result.occasion
        return listOf(color, occasion, result.category)
            .filter { it.isNotBlank() && !it.equals("unknown", true) && !it.equals("undetermined", true) }
            .joinToString(" ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .ifBlank { "Scanned wardrobe item" }
    }

    suspend fun findDuplicateCandidate(result: ClassificationResult): ClothingItem? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val items = wardrobeRepository.getUserItems(userId).getOrDefault(emptyList())
        return items.firstOrNull { item ->
            item.category.equals(result.category, ignoreCase = true) &&
                item.color.equals(result.colour, ignoreCase = true)
        }
    }

    suspend fun uploadScanImage(imageData: ByteArray): Result<String> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return scanRepository.uploadScanImage(userId, imageData)
    }
}
