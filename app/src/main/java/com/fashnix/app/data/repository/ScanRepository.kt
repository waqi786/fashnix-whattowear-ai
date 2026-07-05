package com.fashnix.app.data.repository

import com.fashnix.app.data.model.ScanResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun saveScan(userId: String, scan: ScanResult): Result<ScanResult> {
        return try {
            val scanId = scan.scanId.ifEmpty { UUID.randomUUID().toString() }
            val newScan = scan.copy(scanId = scanId)
            firestore.collection("users").document(userId).collection("scans")
                .document(scanId).set(newScan).await()
            Result.success(newScan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScan(userId: String, scanId: String): Result<ScanResult> {
        return try {
            val doc = firestore.collection("users").document(userId)
                .collection("scans").document(scanId).get().await()
            val scan = doc.toObject(ScanResult::class.java) ?: return Result.failure(Exception("Scan not found"))
            Result.success(scan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentScans(userId: String, limit: Int = 10): Result<List<ScanResult>> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("scans")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val scans = snapshot.toObjects(ScanResult::class.java)
            Result.success(scans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadScanImage(userId: String, imageData: ByteArray): Result<String> {
        return try {
            val path = "scans/$userId/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(path)
            ref.putBytes(imageData).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            // Keep scan flow usable when Firebase Storage is not enabled.
            Result.success("")
        }
    }

    suspend fun deleteScanImage(imageUrl: String): Result<Unit> {
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
}
