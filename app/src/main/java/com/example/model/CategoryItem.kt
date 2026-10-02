package com.example.model

data class CategoryItem(
    val id: String = "",
    val title: String = "",
    val iconName: String = "", // e.g. "games", "apps", "tools", "entertainment", "camera", "chat", "education", "social", "business", "health"
    val colorHex: String = "#3B82F6",
    val isGameCategory: Boolean = false,
    val count: Int = 12
)
