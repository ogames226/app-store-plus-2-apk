package com.example.model

data class UserProfile(
    val uid: String = "user_default",
    val displayName: String = "Admin",
    val email: String = "zaim9002@gmail.com",
    val photoUrl: String? = null,
    val isAdmin: Boolean = true,
    val isCoAdmin: Boolean = false,
    val permissions: List<String> = listOf("ALL"),
    val favorites: List<String> = emptyList()
) {
    fun isAuthorizedAdmin(): Boolean {
        val lower = email.trim().lowercase()
        return isAdmin || isCoAdmin ||
               lower == "zaim9002@gmail.com" ||
               lower == "ogames226@gmail.com" ||
               lower == "houssamtech@gmail.com" ||
               lower.contains("admin")
    }
}

data class AdminMember(
    val email: String,
    val displayName: String,
    val role: String = "Co-Admin", // "Primary Admin", "Co-Admin", "Moderator"
    val addedDate: String = "2026-10-02",
    val canPublish: Boolean = true,
    val canDelete: Boolean = true,
    val canManageCategories: Boolean = true
)
