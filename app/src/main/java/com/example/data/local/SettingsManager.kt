package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.model.AdminMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("appstore_plus_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("app_language", "ar") ?: "ar")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _primaryColorHex = MutableStateFlow(prefs.getString("custom_primary_color", "#3B82F6") ?: "#3B82F6")
    val primaryColorHex: StateFlow<String> = _primaryColorHex.asStateFlow()

    private val _cardStyle = MutableStateFlow(prefs.getString("custom_card_style", "DARK_SLATE") ?: "DARK_SLATE")
    val cardStyle: StateFlow<String> = _cardStyle.asStateFlow()

    private val _coAdmins = MutableStateFlow(getSavedCoAdmins())
    val coAdmins: StateFlow<List<AdminMember>> = _coAdmins.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        val saved = prefs.getString("app_theme_mode", "DARK") ?: "DARK"
        return try {
            ThemeMode.valueOf(saved)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("app_theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _language.value = lang
    }

    fun setPrimaryColor(hex: String) {
        prefs.edit().putString("custom_primary_color", hex).apply()
        _primaryColorHex.value = hex
    }

    fun setCardStyle(style: String) {
        prefs.edit().putString("custom_card_style", style).apply()
        _cardStyle.value = style
    }

    private fun getSavedCoAdmins(): List<AdminMember> {
        val jsonStr = prefs.getString("co_admins_list", null)
        val defaultList = listOf(
            AdminMember("zaim9002@gmail.com", "المدير الرئيسي (Primary Admin)", "Primary Admin", canPublish = true, canDelete = true, canManageCategories = true),
            AdminMember("ogames226@gmail.com", "مشرف المتجر", "Co-Admin", canPublish = true, canDelete = true, canManageCategories = true)
        )
        if (jsonStr.isNullOrBlank()) return defaultList

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AdminMember>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AdminMember(
                        email = obj.getString("email"),
                        displayName = obj.getString("displayName"),
                        role = obj.optString("role", "Co-Admin"),
                        addedDate = obj.optString("addedDate", "2026-10-02"),
                        canPublish = obj.optBoolean("canPublish", true),
                        canDelete = obj.optBoolean("canDelete", true),
                        canManageCategories = obj.optBoolean("canManageCategories", true)
                    )
                )
            }
            if (list.none { it.email.equals("zaim9002@gmail.com", ignoreCase = true) }) {
                list.add(0, defaultList[0])
            }
            list
        } catch (e: Exception) {
            defaultList
        }
    }

    fun addCoAdmin(member: AdminMember) {
        val current = _coAdmins.value.filterNot { it.email.equals(member.email, ignoreCase = true) }.toMutableList()
        current.add(member)
        saveCoAdmins(current)
    }

    fun removeCoAdmin(email: String) {
        if (email.equals("zaim9002@gmail.com", ignoreCase = true)) return // Cannot remove primary admin
        val current = _coAdmins.value.filterNot { it.email.equals(email, ignoreCase = true) }
        saveCoAdmins(current)
    }

    private fun saveCoAdmins(list: List<AdminMember>) {
        val array = JSONArray()
        list.forEach { m ->
            val obj = JSONObject().apply {
                put("email", m.email)
                put("displayName", m.displayName)
                put("role", m.role)
                put("addedDate", m.addedDate)
                put("canPublish", m.canPublish)
                put("canDelete", m.canDelete)
                put("canManageCategories", m.canManageCategories)
            }
            array.put(obj)
        }
        prefs.edit().putString("co_admins_list", array.toString()).apply()
        _coAdmins.value = list
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
