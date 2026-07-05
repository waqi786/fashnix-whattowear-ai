package com.fashnix.app.ui.family

import androidx.lifecycle.ViewModel
import com.fashnix.app.data.model.FamilyGroup
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _group = MutableStateFlow<FamilyGroup?>(null)
    val group = _group.asStateFlow()

    private val _isInGroup = MutableStateFlow(false)
    val isInGroup = _isInGroup.asStateFlow()

    suspend fun loadGroup() {
        val userId = authRepository.getCurrentUserId() ?: return
        val profile = authRepository.getUserProfile(userId).getOrNull() ?: return
        val groupId = profile.familyGroupId
        if (groupId != null) {
            familyRepository.getGroup(groupId).fold(
                onSuccess = { _group.value = it; _isInGroup.value = true },
                onFailure = { _isInGroup.value = false }
            )
        } else {
            _isInGroup.value = false
        }
    }

    suspend fun createGroup(): String? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val userName = authRepository.getUserProfile(userId).getOrNull()?.name ?: ""
        val inviteCode = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
        val group = FamilyGroup(
            groupId = UUID.randomUUID().toString(),
            name = "$userName's Family Closet",
            members = listOf(userId),
            memberNames = mapOf(userId to userName),
            inviteCode = inviteCode
        )
        familyRepository.createGroup(group).onSuccess {
            familyRepository.updateUserFamilyGroupId(userId, group.groupId)
            _group.value = group
            _isInGroup.value = true
            return inviteCode
        }
        return null
    }

    suspend fun joinWithCode(inviteCode: String): Boolean {
        val userId = authRepository.getCurrentUserId() ?: return false
        val userName = authRepository.getUserProfile(userId).getOrNull()?.name ?: ""
        val result = familyRepository.getGroupByInviteCode(inviteCode)
        result.onSuccess { group ->
            familyRepository.addMember(group.groupId, userId, userName)
            familyRepository.updateUserFamilyGroupId(userId, group.groupId)
            _group.value = group
            _isInGroup.value = true
            return true
        }
        return false
    }

    suspend fun leaveGroup() {
        val userId = authRepository.getCurrentUserId() ?: return
        val currentGroup = _group.value ?: return
        familyRepository.removeMember(currentGroup.groupId, userId)
        familyRepository.updateUserFamilyGroupId(userId, null)
        _group.value = null
        _isInGroup.value = false
    }
}