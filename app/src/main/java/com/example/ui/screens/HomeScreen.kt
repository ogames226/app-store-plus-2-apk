package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.ScreenDestination
import com.example.ui.components.AppGridCard
import com.example.ui.components.SearchInputField
import com.example.ui.components.StoreImage
import com.example.ui.components.StoreTopBar
import com.example.ui.components.bannerScrim
import com.example.ui.components.minTouchTarget
import com.example.ui.components.pressable
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ButtonInstallBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Home is the app's landing screen and was a `Column` + `verticalScroll`: every
 * card, both carousels, and all their images were composed up front on first
 * frame, then recomposed in full on every download tick. It's now one
 * `LazyColumn`, so only visible items compose and off-screen ones are disposed.
 */
@Composable
fun HomeScreen(
  storeItems: List<StoreItem>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onInstallClick: (StoreItem) -> Unit,
  onOpenClick: (StoreItem) -> Unit = {},
  onNavigate: (ScreenDestination) -> Unit,
  /** Increments on bottom-nav re-tap of Home; drives scroll-to-top. */
  scrollToTopToken: Int = 0,
  /** Home owns its own query so typing here doesn't tear down the screen. */
  onSearchSubmit: (String) -> Unit = {}
) {
  val listState = rememberLazyListState()
  listState.ScrollToTopOnToken(scrollToTopToken)

  // These two lists were recomputed on every recomposition; they only change
  // when the catalog does.
  val heroItem = remember(storeItems) {
    storeItems.find { it.id == "gta-v" } ?: storeItems.firstOrNull { it.type == "game" }
  }
  val mostDownloaded = remember(storeItems) {
    storeItems.filter { it.type == "app" }.sortedByDescending { it.downloadCount }
  }
  val featuredGames = remember(storeItems) {
    storeItems.filter { it.type == "game" }
  }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    item(key = "top_bar") { StoreTopBar(
      onNotificationClick = { onNavigate(ScreenDestination.Updates) },
      onProfileClick = { onNavigate(ScreenDestination.Profile) },
      onMenuClick = { onNavigate(ScreenDestination.Profile) }
    ) }

    item(key = "search") {
      Spacer(modifier = Modifier.height(6.dp))
      SearchInputField(
        query = searchQuery,
        onQueryChange = onSearchChange,
        onSearchSubmit = onSearchSubmit
      )
    }

    // Hero Featured Banner
    item(key = "hero") {
      if (heroItem != null) {
        Spacer(modifier = Modifier.height(18.dp))
        HeroBanner(
          item = heroItem,
          onClick = { onItemClick(heroItem) },
          onInstallClick = { onInstallClick(heroItem) }
        )
      }
    }

    // 4 Action Buttons Row
    item(key = "quick_actions") {
      Spacer(modifier = Modifier.height(20.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        QuickActionButton(
          title = "تحديثات",
          icon = Icons.Default.SystemUpdate,
          bgColor = Color(0xFF2E192E),
          iconColor = AccentPink,
          onClick = { onNavigate(ScreenDestination.Updates) }
        )

        QuickActionButton(
          title = "تطبيقات",
          icon = Icons.Default.Apps,
          bgColor = Color(0xFF142442),
          iconColor = Color(0xFF3B82F6),
          onClick = { onNavigate(ScreenDestination.Apps) }
        )

        QuickActionButton(
          title = "ألعاب",
          icon = Icons.Default.SportsEsports,
          bgColor = Color(0xFF192548),
          iconColor = Color(0xFF6366F1),
          onClick = { onNavigate(ScreenDestination.Games) }
        )

        QuickActionButton(
          title = "تصنيفات",
          icon = Icons.Default.Download,
          bgColor = Color(0xFF0F3028),
          iconColor = AccentGreen,
          onClick = { onNavigate(ScreenDestination.Categories) }
        )
      }
    }

    // Section "الأكثر تحميلاً"
    item(key = "most_downloaded_header") {
      Spacer(modifier = Modifier.height(26.dp))
      SectionHeader(
        title = "الأكثر تحميلاً",
        onMoreClick = { onNavigate(ScreenDestination.Apps) }
      )
      Spacer(modifier = Modifier.height(12.dp))
    }

    item(key = "most_downloaded_row") {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(mostDownloaded, key = { it.id }) { item ->
          AppGridCard(
            item = item,
            downloadProgress = downloadStates[item.id],
            onClick = { onItemClick(item) },
            onInstallClick = { onInstallClick(item) },
            onOpenClick = { onOpenClick(item) }
          )
        }
      }
    }

    // Section "ألعاب مميزة"
    item(key = "featured_games_header") {
      Spacer(modifier = Modifier.height(28.dp))
      SectionHeader(
        title = "ألعاب مميزة",
        onMoreClick = { onNavigate(ScreenDestination.Games) }
      )
      Spacer(modifier = Modifier.height(12.dp))
    }

    item(key = "featured_games_row") {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(featuredGames, key = { it.id }) { game ->
          FeaturedGameCard(
            game = game,
            onClick = { onItemClick(game) }
          )
        }
      }
    }

    // Trailing space so the last carousel clears the bottom bar without a
    // hardcoded 90dp guess on every screen.
    item(key = "tail") { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun HeroBanner(
  item: StoreItem,
  onClick: () -> Unit,
  onInstallClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .height(190.dp)
      .clip(RoundedCornerShape(22.dp))
      .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
      .pressable(onClick = onClick)
      .testTag("hero_banner_card")
  ) {
    StoreImage(
      data = R.drawable.hero_gta,
      contentDescription = "GTA V Featured",
      // Half the display width: this is a dark scrimmed hero, so the downsample
      // is visually free but removes ~4x the decode work on every Home entry.
      width = 200.dp,
      height = 105.dp,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(bannerScrim())
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = item.name,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
      Text(
        text = "الآن على هاتفك",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFE2E8F0)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = onInstallClick,
        colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
        modifier = Modifier.height(44.dp)
      ) {
        Text(
          text = "تثبيت",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

/** Extracted so the press-scale animation is scoped to this card only. */
@Composable
private fun FeaturedGameCard(
  game: StoreItem,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .width(180.dp)
      .height(115.dp)
      .clip(RoundedCornerShape(18.dp))
      .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
      .pressable(onClick = onClick)
  ) {
    StoreImage(
      data = game.bannerUrl.ifEmpty { R.drawable.screenshot_gta1 },
      contentDescription = game.name,
      width = 180.dp,
      height = 115.dp,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xDD070A12))
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.Bottom
    ) {
      Text(
        text = game.name,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        maxLines = 1
      )
      Text(
        text = game.developer,
        fontSize = 11.sp,
        color = TextSecondary,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  title: String,
  icon: ImageVector,
  bgColor: Color,
  iconColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .pressable(onClick = onClick)
      .padding(vertical = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(62.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(bgColor)
        .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = iconColor,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = TextPrimary
    )
  }
}

@Composable
fun SectionHeader(
  title: String,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 19.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )

    // Was `padding(horizontal = 4.dp, vertical = 2.dp)` on a Text — roughly a
    // 22dp-tall hit area. Now 48dp via minTouchTarget, invisible to layout.
    Text(
      text = "المزيد",
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = PrimaryBlue,
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .pressable(onClick = onMoreClick)
        .minTouchTarget()
    )
  }
}

/**
 * Scroll-to-top hook for bottom-nav re-tap.
 *
 * The parent owns a token that increments whenever the already-active tab is
 * tapped; this watches it and animates the list home. Using a token instead of
 * a boolean means repeated taps keep re-triggering (a bool would fire once).
 */
@Composable
fun LazyListState.ScrollToTopOnToken(token: Int) {
  val handled = remember { mutableIntStateOf(-1) }
  LaunchedEffect(token) {
    if (token > 0 && token != handled.intValue) {
      handled.intValue = token
      animateScrollToItem(0)
    }
  }
}
