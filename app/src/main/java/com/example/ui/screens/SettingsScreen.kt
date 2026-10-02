package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ThemeMode
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentLanguage: String,
    currentPrimaryColor: String,
    currentCardStyle: String,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectPrimaryColor: (String) -> Unit,
    onSelectCardStyle: (String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val colorPalette = listOf(
        "#3B82F6" to "أزرق كلاسيكي",
        "#8B5CF6" to "بنفسجي نيون",
        "#10B981" to "أخضر زمردي",
        "#F97316" to "برتقالي حيوي",
        "#EC4899" to "وردي سايبر",
        "#06B6D4" to "سماوي مضيء"
    )

    val cardStyles = listOf(
        "DARK_SLATE" to "رمادي كحلي داكن",
        "DEEP_BLACK" to "أسود نقي عميق",
        "MIDNIGHT_NAVY" to "أزرق ليلي"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TextPrimary
                )
            }
            Text(
                text = "الإعدادات (Settings)",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 60.dp)
        ) {
            // 1. Language Section
            SettingsSectionHeader(icon = Icons.Filled.Language, title = "اللغة (Language)")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(18.dp))
            ) {
                Column {
                    LanguageOption(
                        title = "العربية (Arabic)",
                        isSelected = currentLanguage == "ar",
                        onClick = { onSelectLanguage("ar") }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    LanguageOption(
                        title = "English (الإنجليزية)",
                        isSelected = currentLanguage == "en",
                        onClick = { onSelectLanguage("en") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Theme Mode Section
            SettingsSectionHeader(icon = Icons.Filled.DarkMode, title = "المظهر (Theme Mode)")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(18.dp))
            ) {
                Column {
                    ThemeOption(
                        title = "الوضع الداكن (Dark)",
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = { onSelectThemeMode(ThemeMode.DARK) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    ThemeOption(
                        title = "الوضع الفاتح (Light)",
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = { onSelectThemeMode(ThemeMode.LIGHT) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    ThemeOption(
                        title = "افتراضي النظام (System Default)",
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = { onSelectThemeMode(ThemeMode.SYSTEM) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Customize Theme (Colors & Cards)
            SettingsSectionHeader(icon = Icons.Filled.ColorLens, title = "تخصيص الألوان (Customize Theme)")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "اللون الأساسي المتفاعل",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(colorPalette) { (hex, name) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = currentPrimaryColor.equals(hex, ignoreCase = true)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onSelectPrimaryColor(hex) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else BorderSubtle,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = name,
                                    color = if (isSelected) color else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "مظهر خلفية البطاقات",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    cardStyles.forEach { (styleKey, title) ->
                        val isSelected = currentCardStyle == styleKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCardStyle(styleKey) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) BrandBlue else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LanguageOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isSelected) BrandBlue else TextPrimary,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ThemeOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isSelected) BrandBlue else TextPrimary,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        }
    }
}
