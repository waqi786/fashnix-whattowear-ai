package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FamilyGroup(
    val groupId: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap(),
    val inviteCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable