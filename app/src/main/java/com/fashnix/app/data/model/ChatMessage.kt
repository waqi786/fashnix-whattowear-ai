package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val id: String = "",
    val role: String = "", // "user" or "assistant"
    val content: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable