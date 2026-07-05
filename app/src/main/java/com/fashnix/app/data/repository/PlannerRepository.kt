package com.fashnix.app.data.repository

import com.fashnix.app.data.model.Outfit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveOutfit(userId: String, outfit: Outfit): Result<Outfit> {
        return try {
            val id = outfit.id.ifEmpty { UUID.randomUUID().toString() }
            val newOutfit = outfit.copy(id = id, userId = userId)
            firestore.collection("planner").document(userId)
                .collection("outfits").document(id)
                .set(newOutfit).await()
            Result.success(newOutfit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOutfitsForDate(userId: String, date: Long): Result<List<Outfit>> {
        return try {
            val snapshot = firestore.collection("planner").document(userId)
                .collection("outfits")
                .whereEqualTo("date", date)
                .get()
                .await()
            val outfits = snapshot.toObjects(Outfit::class.java)
            Result.success(outfits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllOutfits(userId: String): Result<List<Outfit>> {
        return try {
            val snapshot = firestore.collection("planner").document(userId)
                .collection("outfits")
                .get()
                .await()
            val outfits = snapshot.toObjects(Outfit::class.java)
            Result.success(outfits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOutfit(userId: String, outfitId: String): Result<Unit> {
        return try {
            firestore.collection("planner").document(userId)
                .collection("outfits").document(outfitId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markOutfitWorn(userId: String, outfit: Outfit): Result<Unit> {
        return try {
            val updatedOutfit = outfit.copy(isWorn = true)
            firestore.collection("planner").document(userId)
                .collection("outfits").document(outfit.id)
                .set(updatedOutfit).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}