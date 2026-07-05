package com.fashnix.app.data.repository

import com.fashnix.app.data.model.FamilyGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createGroup(group: FamilyGroup): Result<FamilyGroup> {
        return try {
            val docRef = firestore.collection("familyGroups").document(group.groupId)
            docRef.set(group).await()
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupByInviteCode(inviteCode: String): Result<FamilyGroup> {
        return try {
            val snapshot = firestore.collection("familyGroups")
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .await()
            if (snapshot.isEmpty) {
                return Result.failure(Exception("Group not found"))
            }
            val group = snapshot.documents[0].toObject(FamilyGroup::class.java)!!
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroup(groupId: String): Result<FamilyGroup> {
        return try {
            val doc = firestore.collection("familyGroups").document(groupId).get().await()
            val group = doc.toObject(FamilyGroup::class.java) ?: return Result.failure(Exception("Group not found"))
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMember(groupId: String, userId: String, userName: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "members" to FieldValue.arrayUnion(userId),
                "memberNames.$userId" to userName
            )
            firestore.collection("familyGroups").document(groupId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(groupId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("familyGroups").document(groupId).update(
                mapOf(
                    "members" to FieldValue.arrayRemove(userId),
                    "memberNames.$userId" to FieldValue.delete()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            firestore.collection("familyGroups").document(groupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserFamilyGroupId(userId: String, groupId: String?): Result<Unit> {
        return try {
            val updateData = if (groupId != null) mapOf("familyGroupId" to groupId)
            else mapOf("familyGroupId" to FieldValue.delete())
            firestore.collection("users").document(userId).update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}