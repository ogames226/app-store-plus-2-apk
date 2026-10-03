package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.components.AppListItem
import com.example.ui.components.minTouchTarget
import com.example.ui.components.pressable
import com.example.ui.components.SearchInputField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ButtonInstallBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppsScreen(
  apps: List<StoreItem>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedCategory: String,
  onCategorySelect: (String) -> Unit,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onInstallClick: (StoreItem) -> Unit,
  onOpenClick: (StoreItem) -> Unit = {},
  scrollToTopToken: Int = 0,
  catalogState: com.example.ui.CatalogState = com.example.ui.CatalogState.Loading,
  hasMoreItems: Boolean = false,
  isLoadingMore: Boolean = false,
  onLoadMore: () -> Unit = {}
) {
  val listState = rememberLazyListState()
  listState.ScrollToTopOnToken(scrollToTopToken)

  val categories = remember { listOf("الكل", "أدوات", "ترفيه", "تعليم", "تواصل", "تصوير", "اجتماعي", "أعمال", "صحي") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Header Title
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Text(
        text = "التطبيقات",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Search bar
    SearchInputField(
      query = searchQuery,
      onQueryChange = onSearchChange,
      placeholder = "ابحث عن تطبيقات..."
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Categories Horizontal Filter Chips
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories, key = { it }) { cat ->
        val isSelected = cat == selectedCategory
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ButtonInstallBlue else CardDark)
            .border(1.dp, if (isSelected) PrimaryBlue else CardBorder, RoundedCornerShape(20.dp))
            .pressable(onClick = { onCategorySelect(cat) })
            .padding(horizontal = 18.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = cat,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Apps List
    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 16.dp)
    ) {
      items(apps, key = { it.id }) { app ->
        AppListItem(
          item = app,
          downloadProgress = downloadStates[app.id],
          onClick = { onItemClick(app) },
          onInstallClick = { onInstallClick(app) },
          onOpenClick = { onOpenClick(app) }
        )
      }
      if (apps.isEmpty()) {
        item { CatalogPlaceholder(state = catalogState, filtered = searchQuery.isNotBlank() || selectedCategory != "الكل") }
      }

      // Paging footer: the bounded listener only returns the first slice, so
      // reaching the end offers the next page explicitly.
      if (apps.isNotEmpty() && hasMoreItems) {
        item(key = "load_more") {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
          ) {
            if (isLoadingMore) {
              CircularProgressIndicator(
                color = PrimaryBlue,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
              )
            } else {
              Text(
                text = "عرض المزيد",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .pressable(onClick = onLoadMore)
                  .minTouchTarget()
                  .padding(horizontal = 20.dp)
              )
            }
          }
        }
      }
    }
  }
}


/**
 * Renders the honest reason a list is empty.
 *
 * Previously every empty state was one flat "no matches" line and the demo
 * catalog was used as a fallback, so a backend outage looked identical to a
 * successful load with no results.
 */
@Composable
private fun CatalogPlaceholder(
  state: com.example.ui.CatalogState,
  filtered: Boolean
) {
  val (title, subtitle) = when {
    filtered -> "لا توجد نتائج" to "جرّب تعديل البحث أو التصنيف"
    state is com.example.ui.CatalogState.Error ->
      "تعذّر تحميل المتجر" to "تحقق من الاتصال ثم أعد المحاولة"
    state is com.example.ui.CatalogState.Empty ->
      "لا توجد تطبيقات منشورة بعد" to "سيتم عرض التطبيقات فور نشرها"
    else -> "جارٍ التحميل..." to "يتم جلب البيانات من السحابة"
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(40.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      if (state is com.example.ui.CatalogState.Loading) {
        CircularProgressIndicator(
          color = PrimaryBlue,
          modifier = Modifier.size(28.dp),
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
      }
      Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = subtitle, color = TextSecondary, fontSize = 13.sp)
    }
  }
}
