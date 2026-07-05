package com.fashnix.app.domain

import com.fashnix.app.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object StreakManager {

    suspend fun updateStreak(userId: String, db: FirebaseFirestore): List<String> {
        val userDoc = db.collection("users").document(userId).get().await()
        val profile = userDoc.toObject(UserProfile::class.java) ?: return emptyList()

        // Get start of today in millis
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val yesterdayStart = todayStart - 86_400_000L

        // Already updated today
        if (profile.lastStreakDate >= todayStart) {
            return emptyList()
        }

        val newStreak: Int
        val newLongest: Int
        when {
            profile.lastStreakDate >= yesterdayStart -> {
                newStreak = profile.streak + 1
                newLongest = maxOf(profile.longestStreak, newStreak)
            }
            else -> {
                newStreak = 1
                newLongest = profile.longestStreak
            }
        }

        val newBadges = profile.badges.toMutableList()
        val earnedBadges = mutableListOf<String>()

        // Check milestones
        if (newStreak >= 7 && !newBadges.contains("7_day_streaker")) {
            newBadges.add("7_day_streaker")
            earnedBadges.add("7_day_streaker")
        }
        if (newStreak >= 14 && !newBadges.contains("14_day_streaker")) {
            newBadges.add("14_day_streaker")
            earnedBadges.add("14_day_streaker")
        }
        if (newStreak >= 30 && !newBadges.contains("fashion_adventurer")) {
            newBadges.add("fashion_adventurer")
            earnedBadges.add("fashion_adventurer")
        }

        db.collection("users").document(userId).update(
            mapOf(
                "streak" to newStreak,
                "longestStreak" to newLongest,
                "lastStreakDate" to todayStart,
                "badges" to newBadges
            )
        ).await()

        return earnedBadges
    }
}