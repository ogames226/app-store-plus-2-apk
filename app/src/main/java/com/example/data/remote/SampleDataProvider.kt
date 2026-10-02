package com.example.data.remote

import com.example.model.AppItem
import com.example.model.CategoryItem

object SampleDataProvider {

    fun getDefaultCategories(): List<CategoryItem> = listOf(
        // Apps Categories
        CategoryItem("cat_tools", "أدوات", "tools", "#0284C7", isGameCategory = false, count = 0),
        CategoryItem("cat_productivity", "إنتاجية", "business", "#3B82F6", isGameCategory = false, count = 0),
        CategoryItem("cat_education", "تعليم", "education", "#8B5CF6", isGameCategory = false, count = 0),
        CategoryItem("cat_communication", "تواصل", "chat", "#10B981", isGameCategory = false, count = 0),
        CategoryItem("cat_social", "اجتماعي", "social", "#F43F5E", isGameCategory = false, count = 0),
        CategoryItem("cat_entertainment", "ترفيه", "entertainment", "#EC4899", isGameCategory = false, count = 0),
        CategoryItem("cat_music", "موسيقى وصوتيات", "entertainment", "#F59E0B", isGameCategory = false, count = 0),
        CategoryItem("cat_video", "مشغلات ومحررات الفيديو", "entertainment", "#EF4444", isGameCategory = false, count = 0),
        CategoryItem("cat_photography", "تصوير", "camera", "#6366F1", isGameCategory = false, count = 0),
        CategoryItem("cat_personalization", "تخصيص", "tools", "#14B8A6", isGameCategory = false, count = 0),
        CategoryItem("cat_security", "أمان وحماية", "security", "#059669", isGameCategory = false, count = 0),
        CategoryItem("cat_file_mgmt", "إدارة الملفات", "folder", "#D97706", isGameCategory = false, count = 0),
        CategoryItem("cat_browsers", "متصفحات", "web", "#2563EB", isGameCategory = false, count = 0),
        CategoryItem("cat_finance", "مالية", "business", "#047857", isGameCategory = false, count = 0),
        CategoryItem("cat_health", "صحة ولياقة", "health", "#DC2626", isGameCategory = false, count = 0),
        CategoryItem("cat_opensource", "مفتوح المصدر", "code", "#10B981", isGameCategory = false, count = 0),
        CategoryItem("cat_root", "روت ومتقدم", "developer_mode", "#7C3AED", isGameCategory = false, count = 0),
        CategoryItem("cat_android_tools", "أدوات أندرويد", "tools", "#0284C7", isGameCategory = false, count = 0),
        CategoryItem("cat_system_tools", "أدوات النظام", "tools", "#475569", isGameCategory = false, count = 0),
        CategoryItem("cat_utilities", "خدمات عامة", "tools", "#64748B", isGameCategory = false, count = 0),

        // Games Categories
        CategoryItem("cat_action", "أكشن", "gamepad", "#EF4444", isGameCategory = true, count = 0),
        CategoryItem("cat_adventure", "مغامرة", "gamepad", "#F97316", isGameCategory = true, count = 0),
        CategoryItem("cat_arcade", "أركيد", "gamepad", "#EC4899", isGameCategory = true, count = 0),
        CategoryItem("cat_racing", "سباق", "gamepad", "#EAB308", isGameCategory = true, count = 0),
        CategoryItem("cat_simulation", "محاكاة", "gamepad", "#3B82F6", isGameCategory = true, count = 0),
        CategoryItem("cat_sports", "رياضة", "gamepad", "#10B981", isGameCategory = true, count = 0),
        CategoryItem("cat_strategy", "استراتيجية", "gamepad", "#8B5CF6", isGameCategory = true, count = 0),
        CategoryItem("cat_rpg", "تقمص الأدوار (RPG)", "gamepad", "#6366F1", isGameCategory = true, count = 0),
        CategoryItem("cat_puzzle", "ألغاز", "gamepad", "#06B6D4", isGameCategory = true, count = 0),
        CategoryItem("cat_horror", "رعب", "gamepad", "#475569", isGameCategory = true, count = 0),
        CategoryItem("cat_casual", "كاجوال / خفيفة", "gamepad", "#84CC16", isGameCategory = true, count = 0),
        CategoryItem("cat_offline", "ألعاب بدون إنترنت", "gamepad", "#0284C7", isGameCategory = true, count = 0),
        CategoryItem("cat_multiplayer", "ألعاب جماعية", "gamepad", "#D946EF", isGameCategory = true, count = 0),
        CategoryItem("cat_openworld", "عالم مفتوح", "gamepad", "#F43F5E", isGameCategory = true, count = 0),
        CategoryItem("cat_premium", "ألعاب مميزة", "gamepad", "#F59E0B", isGameCategory = true, count = 0)
    )

    // No fake/demo apps: store starts completely clean
    fun getDefaultApps(): List<AppItem> = emptyList()
}
