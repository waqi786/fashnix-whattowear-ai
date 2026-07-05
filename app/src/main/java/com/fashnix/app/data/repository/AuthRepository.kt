package com.fashnix.app.data.repository

import androidx.fragment.app.FragmentActivity
import com.fashnix.app.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("User not found"))
            val document = firestore.collection("users").document(userId).get().await()
            val profile = document.toObject(UserProfile::class.java) ?: UserProfile(userId = userId, email = email)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(name: String, email: String, password: String, gender: String, bodyType: String): Result<UserProfile> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(Exception("User creation failed"))
            val profile = UserProfile(
                userId = userId,
                name = name,
                email = email,
                gender = gender,
                bodyType = bodyType,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithOAuthProvider(activity: FragmentActivity, providerId: String): Result<UserProfile> {
        return try {
            val provider = OAuthProvider.newBuilder(providerId).build()
            val authResult = auth.startActivityForSignInWithProvider(activity, provider).await()
            val user = authResult.user ?: return Result.failure(Exception("Provider sign-in failed"))
            val profile = UserProfile(
                userId = user.uid,
                name = user.displayName.orEmpty(),
                email = user.email.orEmpty(),
                gender = "Unisex",
                bodyType = "Athletic",
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(user.uid).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val profile = document.toObject(UserProfile::class.java) ?: return Result.failure(Exception("Profile not found"))
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return callbackFlow {
            val subscription = firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        trySend(snapshot.toObject(UserProfile::class.java))
                    } else {
                        trySend(null)
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(profile.userId).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(userId: String, imageData: ByteArray): Result<String> {
        return try {
            val path = "profiles/$userId/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(path)
            ref.putBytes(imageData).await()
            Result.success(ref.downloadUrl.await().toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
