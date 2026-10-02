package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DarkBg = Color(0xFF0B0F19)
val SurfaceDark = Color(0xFF13192B)
val SurfaceVariantDark = Color(0xFF1A2238)
val SurfaceCard = Color(0xFF151C30)
val BorderSubtle = Color(0xFF1F2942)

val BrandBlue = Color(0xFF3B82F6)
val BrandBlueDark = Color(0xFF2563EB)
val BrandPurple = Color(0xFF8B5CF6)
val BrandIndigo = Color(0xFF6366F1)
val BrandPink = Color(0xFFEC4899)
val BrandGreen = Color(0xFF10B981)
val BrandOrange = Color(0xFFF97316)
val BrandRed = Color(0xFFEF4444)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
)

val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
)

val CardGlow = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E2942), Color(0xFF13192B))
)
