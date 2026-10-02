package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppGridCard(
    app: AppItem,
    downloadItem: DownloadItem?,
    onClick: () -> Unit,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            AppIcon(
                app = app,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = app.downloadsCount,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DownloadButton(
                downloadItem = downloadItem,
                defaultText = "تثبيت",
                onInstallClick = onInstallClick,
                onCancelClick = onCancelClick,
                onOpenClick = onOpenClick,
                height = 28.dp,
                fontSize = 11,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppListRow(
    app: AppItem,
    downloadItem: DownloadItem?,
    subtitle: String? = null,
    buttonText: String = "تثبيت",
    onClick: () -> Unit,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        AppIcon(
            app = app,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle ?: app.developer,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (buttonText == "تحديث") {
                    Text(
                        text = "★ ${app.newVersion.ifBlank { app.version }}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "★ ${app.downloadsCount}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Action Button
        DownloadButton(
            downloadItem = downloadItem,
            defaultText = buttonText,
            onInstallClick = onInstallClick,
            onCancelClick = onCancelClick,
            onOpenClick = onOpenClick,
            height = 32.dp,
            fontSize = 12
        )
    }
}

@Composable
fun AppIcon(
    app: AppItem,
    modifier: Modifier = Modifier
) {
    if (app.localIconRes != null) {
        Image(
            painter = painterResource(id = app.localIconRes),
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (app.iconUrl.isNotBlank()) {
        AsyncImage(
            model = app.iconUrl,
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Fallback default icon
        Image(
            painter = painterResource(id = R.drawable.img_app_icon),
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
