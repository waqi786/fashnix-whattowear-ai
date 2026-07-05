package com.fashnix.app.data.repository

import com.fashnix.app.data.model.ClothingItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun addItem(item: ClothingItem): Result<ClothingItem> {
        return try {
            val id = item.id.ifEmpty { UUID.randomUUID().toString() }
            val newItem = item.copy(id = id)
            firestore.collection("wardrobe").document(id).set(newItem).await()
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItem(itemId: String): Result<ClothingItem> {
        return try {
            val document = firestore.collection("wardrobe").document(itemId).get().await()
            val item = document.toObject(ClothingItem::class.java) ?: return Result.failure(Exception("Item not found"))
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserItems(userId: String): Result<List<ClothingItem>> {
        return try {
            val snapshot = firestore.collection("wardrobe")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val items = snapshot.toObjects(ClothingItem::class.java)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(item: ClothingItem): Result<Unit> {
        return try {
            firestore.collection("wardrobe").document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            val existingItem = firestore.collection("wardrobe").document(itemId).get().await()
                .toObject(ClothingItem::class.java)
            firestore.collection("wardrobe").document(itemId).delete().await()
            existingItem?.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                runCatching { storage.getReferenceFromUrl(imageUrl).delete().await() }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(userId: String, imageData: ByteArray): Result<String> {
        return try {
            val path = "wardrobe/$userId/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(path)
            ref.putBytes(imageData).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            // Firebase Storage may be unavailable on Spark projects until the bucket is created.
            // Keep wardrobe creation usable; UI will render the local placeholder safely.
            Result.success("")
        }
    }

    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            if (imageUrl.isNotEmpty()) {
                val ref = storage.getReferenceFromUrl(imageUrl)
                ref.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyItems(userIds: List<String>): Result<List<ClothingItem>> {
        return try {
            if (userIds.isEmpty()) return Result.success(emptyList())
            val snapshot = firestore.collection("wardrobe")
                .whereIn("userId", userIds)
                .get()
                .await()
            val items = snapshot.toObjects(ClothingItem::class.java)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
