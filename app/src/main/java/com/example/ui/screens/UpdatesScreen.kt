package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.components.AppListRow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun UpdatesScreen(
    updates: List<AppItem>,
    downloads: Map<String, DownloadItem>,
    onAppClick: (AppItem) -> Unit,
    onUpdateApp: (AppItem) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenApp: (AppItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "التحديثات",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "التطبيقات والألعاب التي تحتاج إلى تحديث",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // List of items to update
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 85.dp)
        ) {
            items(updates) { app ->
                AppListRow(
                    app = app,
                    downloadItem = downloads[app.id],
                    subtitle = app.developer,
                    buttonText = "تحديث",
                    onClick = { onAppClick(app) },
                    onInstallClick = { onUpdateApp(app) },
                    onCancelClick = { onCancelDownload(app.id) },
                    onOpenClick = { onOpenApp(app) }
                )
            }
        }
    }
}
