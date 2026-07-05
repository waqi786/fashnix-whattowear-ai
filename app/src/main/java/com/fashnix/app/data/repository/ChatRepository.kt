package com.fashnix.app.data.repository

import com.fashnix.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveMessage(userId: String, message: ChatMessage): Result<ChatMessage> {
        return try {
            val id = message.id.ifEmpty { UUID.randomUUID().toString() }
            val newMessage = message.copy(id = id)
            firestore.collection("users").document(userId)
                .collection("chatHistory").document(id)
                .set(newMessage).await()
            Result.success(newMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(
        userId: String,
        limit: Int = 50,
        startAfterTimestamp: Long? = null
    ): Result<List<ChatMessage>> {
        return try {
            var query: Query = firestore.collection("users").document(userId)
                .collection("chatHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startAfterTimestamp != null) {
                query = query.startAfter(startAfterTimestamp)
            }

            val snapshot = query.get().await()
            val messages = snapshot.toObjects(ChatMessage::class.java)
            Result.success(messages.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearChat(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("chatHistory")
                .get()
                .await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}