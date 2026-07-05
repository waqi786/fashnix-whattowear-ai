package com.fashnix.app.domain

import com.fashnix.app.data.model.ScanResult
import com.fashnix.app.utils.SampleData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SampleDataHelper {
    private const val SAMPLE_IMAGE = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=900&q=80"

    private fun createSampleScan(): ScanResult {
        return ScanResult(
            scanId = "sample_scan_1",
            category = "Apparel",
            colour = "Blue",
            occasion = "Casual",
            gender = "Men",
            categoryConfidence = 0.96f,
            colourConfidence = 0.92f,
            occasionConfidence = 0.88f,
            genderConfidence = 0.94f,
            isColourOther = false,
            imageUrl = SAMPLE_IMAGE,
            stylingTips = listOf(
                "Layer with a jacket or cardigan for versatility.",
                "Comfort is key - choose breathable fabrics.",
                "Complement with orange or gold accents."
            )
        )
    }

    suspend fun insertSampleData(userId: String, firestore: FirebaseFirestore) {
        try {
            for (item in SampleData.getSampleItems(userId)) {
                firestore.collection("wardrobe").document(item.id).set(item).await()
            }

            val scan = createSampleScan()
            firestore.collection("users").document(userId)
                .collection("scans").document(scan.scanId)
                .set(scan).await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
