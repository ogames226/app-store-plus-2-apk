package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.components.InstallButton
import com.example.ui.components.StoreImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DetailScreen(
  item: StoreItem,
  downloadProgress: DownloadProgress?,
  onBack: () -> Unit,
  onInstallClick: (StoreItem) -> Unit,
  onOpenClick: (StoreItem) -> Unit = {}
) {
  BackHandler { onBack() }

  val listState = rememberLazyListState()
  val context = LocalContext.current
  val isGame = item.type == "game"

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark),
    contentPadding = PaddingValues(bottom = 40.dp)
  ) {
    item(key = "top_bar") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Ø±Ø¬ÙØ¹",
            tint = TextPrimary
          )
        }

        IconButton(
          onClick = { shareItem(context, item) },
          modifier = Modifier.testTag("detail_share_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "ÙØ´Ø§Ø±ÙØ©",
            tint = TextPrimary
          )
        }
      }
    }

    if (isGame) {
      item(key = "game_banner") {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        ) {
          StoreImage(
            data = item.bannerUrl.ifEmpty { R.drawable.hero_gta },
            contentDescription = item.name,
            width = 360.dp,
            height = 200.dp
          )
        }
      }

      item(key = "game_title") {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(CardDark)
              .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
          ) {
            StoreImage(
              data = item.iconUrl.ifEmpty { R.drawable.hero_gta },
              contentDescription = item.name,
              width = 72.dp,
              height = 72.dp
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.name,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(text = item.developer, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "${item.rating} • ${item.fileSize}",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }
        }
      }
    } else {
      item(key = "app_header") {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(CardDark)
              .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
          ) {
            StoreImage(
              data = item.iconUrl.ifEmpty { R.drawable.app_logo },
              contentDescription = item.name,
              width = 100.dp,
              height = 100.dp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = item.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(text = item.developer, fontSize = 14.sp, color = TextSecondary)
          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatColumn(value = "${item.rating} ★", label = "التقييم")
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(CardBorder)
            )
            StatColumn(value = item.downloads, label = "التحميل")
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(CardBorder)
            )
            StatColumn(value = item.fileSize, label = "الحجم")
          }
        }
      }
    }

    item(key = "install") {
      Spacer(modifier = Modifier.height(20.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
      ) {
        InstallButton(
          item = item,
          downloadProgress = downloadProgress,
          onInstallClick = { onInstallClick(item) },
          onOpenClick = { onOpenClick(item) },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          isSmall = false
        )
      }
    }

    item(key = "tags") {
      if (item.categoryTags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item.categoryTags.forEach { tag ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
              Text(text = tag, fontSize = 12.sp, color = TextSecondary)
            }
          }
        }
      }
    }

    item(key = "screens") {
      val displayScreenshots = if (item.screenshots.isNotEmpty()) {
        item.screenshots
      } else if (isGame) {
        listOf(
          "android.resource://${LocalContext.current.packageName}/drawable/screenshot_gta1",
          "android.resource://${LocalContext.current.packageName}/drawable/screenshot_gta2"
        )
      } else {
        listOf("android.resource://${LocalContext.current.packageName}/drawable/screenshot_wa1")
      }

      Spacer(modifier = Modifier.height(20.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        displayScreenshots.forEach { img ->
          Box(
            modifier = Modifier
              .width(if (isGame) 230.dp else 140.dp)
              .height(if (isGame) 130.dp else 220.dp)
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
          ) {
            StoreImage(
              data = img,
              contentDescription = "لقطة شاشة",
              width = if (isGame) 230.dp else 140.dp,
              height = if (isGame) 130.dp else 220.dp
            )
          }
        }
      }
    }

    item(key = "about") {
      Spacer(modifier = Modifier.height(24.dp))
      Text(
        text = if (isGame) "نبذة عن اللعبة" else "نبذة عن التطبيق",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = item.description,
        fontSize = 14.sp,
        color = TextSecondary,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
      )

      if (item.modInfo.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1B2E))
            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(16.dp)
        ) {
          Text(
            text = "معلومات التعديل والميزات الإضافية (Mod Info)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA78BFA)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = item.modInfo,
            fontSize = 13.sp,
            color = Color(0xFFE2E8F0),
            lineHeight = 20.sp
          )
        }
      }
    }

    item(key = "info") {
      Spacer(modifier = Modifier.height(24.dp))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(CardDark)
          .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
          .padding(16.dp)
      ) {
        Text(
          text = "معلومات إضافية",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(label = "الإصدار", value = "${item.version} (${item.versionCode})")
        InfoRow(label = "حجم الملف", value = item.fileSize)
        InfoRow(label = "تاريخ التحديث", value = item.updatedDate.ifEmpty { "2026-10-01" })
        InfoRow(label = "التوافق", value = item.compatibility)
        if (item.targetSdk.isNotBlank()) {
          InfoRow(label = "المستوى المستهدف", value = item.targetSdk)
        }
        if (item.cpuArchitecture.isNotBlank()) {
          InfoRow(label = "معمارية المعالج (ABI)", value = item.cpuArchitecture)
        }
        InfoRow(label = "اسم الحزمة", value = item.packageName)
      }
    }
  }
}

@Composable
private fun StatColumn(value: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 12.sp,
      color = TextSecondary
    )
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = TextMuted
    )
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = TextPrimary
    )
  }
}


/** Real share intent — replaces the two previously inert share/more controls. */
private fun shareItem(context: android.content.Context, item: com.example.model.StoreItem) {
  val text = buildString {
    append(item.name)
    if (item.developer.isNotBlank()) append(" — ").append(item.developer)
    if (item.rating > 0) append("\n★ ").append(item.rating)
    if (item.fileSize.isNotBlank()) append("\nالحجم: ").append(item.fileSize)
  }
  val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(android.content.Intent.EXTRA_TEXT, text)
  }
  context.startActivity(android.content.Intent.createChooser(intent, "مشاركة عبر"))
}
