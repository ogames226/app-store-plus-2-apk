package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.TextMuted

enum class NavTab(val title: String) {
    HOME("الرئيسية"),
    GAMES("الألعاب"),
    APPS("التطبيقات"),
    UPDATES("التحديثات"),
    MORE("المزيد")
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    updatesCount: Int = 0,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkBg,
        border = BorderStroke(0.5.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    tab = NavTab.UPDATES,
                    isSelected = selectedTab == NavTab.UPDATES,
                    icon = Icons.Outlined.SystemUpdate,
                    selectedIcon = Icons.Filled.SystemUpdate,
                    badgeCount = updatesCount,
                    onClick = { onTabSelected(NavTab.UPDATES) }
                )
                NavItem(
                    tab = NavTab.GAMES,
                    isSelected = selectedTab == NavTab.GAMES,
                    icon = Icons.Outlined.SportsEsports,
                    selectedIcon = Icons.Filled.SportsEsports,
                    badgeCount = 0,
                    onClick = { onTabSelected(NavTab.GAMES) }
                )
                NavItem(
                    tab = NavTab.APPS,
                    isSelected = selectedTab == NavTab.APPS,
                    icon = Icons.Outlined.GridView,
                    selectedIcon = Icons.Filled.GridView,
                    badgeCount = 0,
                    onClick = { onTabSelected(NavTab.APPS) }
                )
                NavItem(
                    tab = NavTab.HOME,
                    isSelected = selectedTab == NavTab.HOME,
                    icon = Icons.Outlined.Home,
                    selectedIcon = Icons.Filled.Home,
                    badgeCount = 0,
                    onClick = { onTabSelected(NavTab.HOME) }
                )
                NavItem(
                    tab = NavTab.MORE,
                    isSelected = selectedTab == NavTab.MORE,
                    icon = Icons.Outlined.Menu,
                    selectedIcon = Icons.Filled.Menu,
                    badgeCount = 0,
                    onClick = { onTabSelected(NavTab.MORE) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    isSelected: Boolean,
    icon: ImageVector,
    selectedIcon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val color = if (isSelected) BrandBlue else TextMuted
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = tab.title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(BrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            text = tab.title,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
