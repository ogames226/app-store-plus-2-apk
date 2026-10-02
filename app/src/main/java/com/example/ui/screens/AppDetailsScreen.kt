package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.components.AppIcon
import com.example.ui.components.DownloadButton
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppDetailsScreen(
    app: AppItem,
    downloadItem: DownloadItem?,
    onBackClick: () -> Unit,
    onInstallApp: (AppItem) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenApp: (AppItem) -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current

    val shareApp = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, app.name)
            putExtra(
                Intent.EXTRA_TEXT,
                "حمل تطبيق ${app.name} من App Store Plus: ${app.apkDownloadUrl}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = TextPrimary
                    )
                }

                Row {
                    IconButton(onClick = shareApp) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "مشاركة",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "المزيد",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Mode 1: Game Details (like GTA V in Image 4) with Hero Banner
        if (app.isGame) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = app.localBannerRes ?: R.drawable.img_gtav_banner),
                        contentDescription = app.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xB30B0F19))
                                )
                            )
                    )
                }
            }

            // Header info row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        app = app,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = app.name,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = app.developer,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${app.rating}  •  ${app.fileSize}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Mode 2: App Details (like WhatsApp in Image 7) with Centered Big Icon & Stats Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppIcon(
                        app = app,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(22.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = app.name,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = app.developer,
                        color = TextMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3-Column Stats Row: التقييم | التحميل | الحجم
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${app.rating} ★",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "التقييم",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderSubtle))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = app.downloadsCount,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "التحميل",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderSubtle))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = app.fileSize,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الحجم",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Big Primary "تثبيت" / "تحديث" Full Width Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                DownloadButton(
                    downloadItem = downloadItem,
                    defaultText = if (app.isUpdateAvailable) "تحديث" else "تثبيت",
                    onInstallClick = { onInstallApp(app) },
                    onCancelClick = { onCancelDownload(app.id) },
                    onOpenClick = { onOpenApp(app) },
                    height = 50.dp,
                    fontSize = 16,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Special Badges (Open Source, Root & Advanced, Mod info)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (app.isOpenSource) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF064E3B))
                            .border(width = 0.5.dp, color = Color(0xFF10B981), shape = RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "مفتوح المصدر (Open Source)",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (app.rootCompatibility != "No root required") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF3B0764))
                            .border(width = 0.5.dp, color = Color(0xFFA855F7), shape = RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = app.rootCompatibility,
                            color = Color(0xFFC084FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tags Pill Row (e.g., عالم مفتوح, أكشن, مغامرة)
        if (app.tags.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    app.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceCard)
                                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Screenshots Carousel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf(1, 2, 3)) { idx ->
                        Box(
                            modifier = Modifier
                                .width(if (app.isGame) 240.dp else 130.dp)
                                .height(if (app.isGame) 140.dp else 220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                        ) {
                            if (app.isGame) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_gtav_banner),
                                    contentDescription = "Screenshot $idx",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Stylized preview for app
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SurfaceCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        AppIcon(
                                            app = app,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "معاينة $idx",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: "نبذة عن اللعبة" / "نبذة عن التطبيق"
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (app.isGame) "نبذة عن اللعبة" else "نبذة عن التطبيق",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = app.description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // Additional Information section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "معلومات إضافية",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                InfoRow(label = "اسم الحزمة", value = app.packageName)
                InfoRow(label = "الإصدار", value = app.version)
                InfoRow(label = "كود الإصدار", value = app.versionCode.toString())
                InfoRow(label = "المعمارية (ABI)", value = app.abi)
                InfoRow(label = "الحد الأدنى للنظام", value = "Android ${app.minSdk}+")
                InfoRow(label = "تاريخ التحديث", value = app.updatedDate)
                InfoRow(label = "المطور", value = app.developer)
                InfoRow(label = "حجم التنزيل", value = app.fileSize)
                if (app.modInfo.isNotBlank()) {
                    InfoRow(label = "معلومات التعديل", value = app.modInfo)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
