package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.components.AppListRow
import com.example.ui.components.AppSearchBar
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AppsScreen(
    apps: List<AppItem>,
    downloads: Map<String, DownloadItem>,
    isGamesOnly: Boolean = false,
    initialCategory: String? = null,
    onAppClick: (AppItem) -> Unit,
    onInstallApp: (AppItem) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenApp: (AppItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialCategory ?: "الكل") }

    val filterChips = if (isGamesOnly) {
        listOf("الكل", "أكشن", "مغامرة", "عالم مفتوح", "إطلاق نار")
    } else {
        listOf("الكل", "أدوات", "ترفيه", "تعليم", "تواصل", "اجتماعي", "أعمال")
    }

    val filteredApps = apps.filter { app ->
        val matchesCategory = when (selectedFilter) {
            "الكل" -> true
            else -> app.category.contains(selectedFilter, ignoreCase = true) ||
                    app.tags.any { it.contains(selectedFilter, ignoreCase = true) }
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            app.name.contains(searchQuery, ignoreCase = true) ||
            app.developer.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Title: التطبيقات / الألعاب
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (isGamesOnly) "الألعاب" else "التطبيقات",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = if (isGamesOnly) "ابحث عن ألعاب..." else "ابحث عن تطبيقات..."
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Filter Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filterChips) { chip ->
                val isSelected = selectedFilter == chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandBlue else SurfaceCard)
                        .border(
                            width = 0.5.dp,
                            color = if (isSelected) BrandBlue else BorderSubtle,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = chip }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chip,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vertical List of Apps
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 85.dp)
        ) {
            items(filteredApps) { app ->
                AppListRow(
                    app = app,
                    downloadItem = downloads[app.id],
                    onClick = { onAppClick(app) },
                    onInstallClick = { onInstallApp(app) },
                    onCancelClick = { onCancelDownload(app.id) },
                    onOpenClick = { onOpenApp(app) }
                )
            }
        }
    }
}
