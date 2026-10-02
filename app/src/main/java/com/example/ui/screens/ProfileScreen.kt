package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOutClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 70.dp)
    ) {
        // Title: حسابي
        Text(
            text = "حسابي",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // User Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar),
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandBlue, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userProfile.displayName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userProfile.email,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    if (userProfile.isAdmin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E3A8A))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مدير المتجر (Admin)",
                                color = BrandBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options Menu Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(20.dp))
        ) {
            Column {
                MenuItem(
                    icon = Icons.Filled.PersonOutline,
                    title = "الملف الشخصي",
                    onClick = onSignInClick
                )
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                MenuItem(
                    icon = Icons.Filled.BookmarkBorder,
                    title = "المفضلة",
                    onClick = {}
                )
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                // Admin Dashboard Option
                MenuItem(
                    icon = Icons.Filled.AdminPanelSettings,
                    title = "لوحة تحكم المدير (Admin Dashboard)",
                    iconTint = BrandBlue,
                    onClick = onNavigateToAdmin
                )
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                MenuItem(
                    icon = Icons.Filled.Settings,
                    title = "الإعدادات",
                    onClick = onNavigateToSettings
                )
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                MenuItem(
                    icon = Icons.Filled.HelpOutline,
                    title = "المساعدة والدعم",
                    onClick = {}
                )
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                MenuItem(
                    icon = Icons.Filled.Info,
                    title = "حول التطبيق",
                    subtitle = "App Store Plus v1.0.0",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Logout Button matching Image 8
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0x1AEF4444))
                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(26.dp))
                .clickable(onClick = onSignOutClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "تسجيل الخروج",
                color = BrandRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
