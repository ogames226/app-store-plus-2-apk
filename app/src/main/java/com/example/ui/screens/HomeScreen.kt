package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.ui.components.AppGridCard
import com.example.ui.components.AppSearchBar
import com.example.ui.components.DownloadButton
import com.example.ui.components.QuickActionPill
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    featuredGame: AppItem?,
    mostDownloadedApps: List<AppItem>,
    featuredGames: List<AppItem>,
    downloads: Map<String, DownloadItem>,
    onAppClick: (AppItem) -> Unit,
    onInstallApp: (AppItem) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenApp: (AppItem) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToUpdates: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Bar: Notification Bell & Profile Avatar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notifications icon with badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .clickable { onNavigateToUpdates() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Profile Avatar button
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, BrandBlue, CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Search Bar (Read-only click to search screen)
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                AppSearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = "ابحث عن تطبيقات أو ألعاب...",
                    isReadOnly = true,
                    onClickWhenReadOnly = onSearchClick
                )
            }
        }

        // Featured Hero Banner
        item {
            if (featuredGame != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onAppClick(featuredGame) }
                ) {
                    if (featuredGame.localBannerRes != null) {
                        Image(
                            painter = painterResource(id = featuredGame.localBannerRes),
                            contentDescription = featuredGame.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_gtav_banner),
                            contentDescription = featuredGame.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradient shade for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xF50B0F19))
                                )
                            )
                    )

                    // Content overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = featuredGame.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الآن على هاتفك",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DownloadButton(
                            downloadItem = downloads[featuredGame.id],
                            defaultText = "تثبيت",
                            onInstallClick = { onInstallApp(featuredGame) },
                            onCancelClick = { onCancelDownload(featuredGame.id) },
                            onOpenClick = { onOpenApp(featuredGame) },
                            height = 32.dp,
                            fontSize = 12
                        )
                    }
                }
            } else {
                // Clean empty-store hero banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "App Store Plus",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المتجر خالٍ من البيانات الوهمية وجاهز لرفع تطبيقاتك الحقيقية",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رفع ونشر أول تطبيق من لوحة التحكم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4 Quick Category Pills Row: تحديثات, تطبيقات, ألعاب, تصنيفات
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionPill(
                    title = "تصنيفات",
                    icon = Icons.Filled.Folder,
                    iconColor = Color(0xFF10B981),
                    bgColor = Color(0xFF064E3B),
                    onClick = onNavigateToCategories
                )
                QuickActionPill(
                    title = "ألعاب",
                    icon = Icons.Filled.SportsEsports,
                    iconColor = Color(0xFF3B82F6),
                    bgColor = Color(0xFF1E3A8A),
                    onClick = onNavigateToGames
                )
                QuickActionPill(
                    title = "تطبيقات",
                    icon = Icons.Filled.GridView,
                    iconColor = Color(0xFF8B5CF6),
                    bgColor = Color(0xFF4C1D95),
                    onClick = onNavigateToApps
                )
                QuickActionPill(
                    title = "تحديثات",
                    icon = Icons.Filled.Update,
                    iconColor = Color(0xFFEC4899),
                    bgColor = Color(0xFF831843),
                    onClick = onNavigateToUpdates
                )
            }
        }

        // Section: الأكثر تحميلاً (Most Downloaded)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأكثر تحميلاً",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "المزيد",
                    color = BrandBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToApps() }
                )
            }
        }

        item {
            if (mostDownloadedApps.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mostDownloadedApps) { app ->
                        AppGridCard(
                            app = app,
                            downloadItem = downloads[app.id],
                            onClick = { onAppClick(app) },
                            onInstallClick = { onInstallApp(app) },
                            onCancelClick = { onCancelDownload(app.id) },
                            onOpenClick = { onOpenApp(app) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تطبيقات منشورة حالياً في هذا القسم. ارفع تطبيقاً وضع علامة \"الأكثر تحميلاً\".",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Section: ألعاب مميزة (Featured Games)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ألعاب مميزة",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "المزيد",
                    color = BrandBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToGames() }
                )
            }
        }

        // Horizontal Carousel of Featured Games
        item {
            if (featuredGames.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(featuredGames) { game ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                                .clickable { onAppClick(game) }
                        ) {
                            if (game.localBannerRes != null) {
                                Image(
                                    painter = painterResource(id = game.localBannerRes),
                                    contentDescription = game.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xB3000000))
                                        )
                                    )
                            )
                            Text(
                                text = game.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد ألعاب مميزة منشورة حالياً. ارفع لعبة وضع علامة \"مميز في الصفحة الرئيسية\".",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
