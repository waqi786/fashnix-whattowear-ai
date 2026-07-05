package com.fashnix.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.fragment.app.FragmentActivity
import com.fashnix.app.data.model.UserProfile
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.WardrobeRepository
import com.fashnix.app.domain.SampleDataHelper
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    suspend fun login(email: String, password: String): Result<UserProfile> {
        return authRepository.login(email, password)
    }

    suspend fun signup(name: String, email: String, password: String, gender: String, bodyType: String): Result<UserProfile> {
        val result = authRepository.signup(name, email, password, gender, bodyType)
        result.getOrNull()?.let { profile ->
            // Insert sample data
            SampleDataHelper.insertSampleData(profile.userId, firestore)
        }
        return result
    }

    suspend fun loginWithOAuthProvider(activity: FragmentActivity, providerId: String): Result<UserProfile> {
        return authRepository.loginWithOAuthProvider(activity, providerId)
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun logout() {
        authRepository.logout()
    }
}
