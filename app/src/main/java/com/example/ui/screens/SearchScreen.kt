package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.components.AppListRow
import com.example.ui.components.AppSearchBar
import com.example.ui.theme.DarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SearchScreen(
    allApps: List<AppItem>,
    downloads: Map<String, DownloadItem>,
    onAppClick: (AppItem) -> Unit,
    onInstallApp: (AppItem) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenApp: (AppItem) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    var query by remember { mutableStateOf("") }

    val results = if (query.isBlank()) emptyList() else {
        val q = query.trim().lowercase()
        allApps.filter {
            it.name.lowercase().contains(q) ||
            it.developer.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Search Input Header with Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AppSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "ابحث عن تطبيقات أو ألعاب..."
                )
            }
        }

        if (query.isNotBlank()) {
            Text(
                text = "النتائج (${results.size})",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (query.isNotBlank() && results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم العثور على نتائج لـ \"$query\"",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp)
            ) {
                items(results) { app ->
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
}
