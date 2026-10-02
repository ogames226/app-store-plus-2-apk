package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminMember
import com.example.model.AppItem
import com.example.model.CategoryItem
import com.example.ui.components.AppIcon
import com.example.ui.components.AppSearchBar
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ApkParser
import com.example.util.ExtractedApkData
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    apps: List<AppItem>,
    categories: List<CategoryItem>,
    coAdmins: List<AdminMember>,
    onSaveApp: (AppItem) -> Unit,
    onDeleteApp: (String) -> Unit,
    onTogglePublish: (String, Boolean) -> Unit,
    onSaveCategory: (CategoryItem) -> Unit,
    onDeleteCategory: (CategoryItem) -> Unit,
    onAddCoAdmin: (AdminMember) -> Unit,
    onRemoveCoAdmin: (String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingApp by remember { mutableStateOf<AppItem?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddAdminDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "لوحة تحكم المدير (Admin Dashboard)",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "المدير: zaim9002@gmail.com",
                    color = BrandBlue,
                    fontSize = 12.sp
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = BrandBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandBlue
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("الإحصائيات", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("المحتوى (${apps.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("التصنيفات (${categories.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("المشرفين (${coAdmins.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> AdminStatsView(
                apps = apps,
                categories = categories,
                onAddNewApp = { showAddAppDialog = true },
                onAddNewCategory = { showAddCategoryDialog = true }
            )
            1 -> AdminAppsListView(
                apps = apps,
                onEdit = { editingApp = it },
                onDelete = onDeleteApp,
                onTogglePublish = onTogglePublish,
                onAddNew = { showAddAppDialog = true }
            )
            2 -> AdminCategoriesListView(
                categories = categories,
                onDelete = onDeleteCategory,
                onAddNew = { showAddCategoryDialog = true }
            )
            3 -> AdminCoAdminsListView(
                coAdmins = coAdmins,
                onAddAdmin = { showAddAdminDialog = true },
                onRemoveAdmin = onRemoveCoAdmin
            )
        }
    }

    // Add or Edit App Dialog with Real APK Extractor
    if (showAddAppDialog || editingApp != null) {
        RealApkUploadDialog(
            app = editingApp ?: AppItem(
                id = "app_${UUID.randomUUID().toString().take(8)}",
                category = categories.firstOrNull()?.title ?: "أدوات"
            ),
            categories = categories,
            onDismiss = {
                showAddAppDialog = false
                editingApp = null
            },
            onSave = { updated ->
                onSaveApp(updated)
                showAddAppDialog = false
                editingApp = null
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { cat ->
                onSaveCategory(cat)
                showAddCategoryDialog = false
            }
        )
    }

    // Add Co-Admin Dialog
    if (showAddAdminDialog) {
        AddCoAdminDialog(
            onDismiss = { showAddAdminDialog = false },
            onSave = { admin ->
                onAddCoAdmin(admin)
                showAddAdminDialog = false
            }
        )
    }
}

@Composable
private fun AdminStatsView(
    apps: List<AppItem>,
    categories: List<CategoryItem>,
    onAddNewApp: () -> Unit,
    onAddNewCategory: () -> Unit
) {
    val totalApps = apps.count { !it.isGame }
    val totalGames = apps.count { it.isGame }
    val publishedCount = apps.count { it.isPublished }
    val openSourceCount = apps.count { it.isOpenSource }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "نظرة عامة على المتجر",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of 4 Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "التطبيقات",
                value = totalApps.toString(),
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "الألعاب",
                value = totalGames.toString(),
                color = BrandPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "المنشورة في المتجر",
                value = publishedCount.toString(),
                color = BrandGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "مفتوحة المصدر",
                value = openSourceCount.toString(),
                color = BrandPink,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "إجراءات الإدارة",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddNewApp,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("رفع ملف APK ونشر تطبيق جديد", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddNewCategory,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Category, contentDescription = null, tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة تصنيف جديد", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = title, color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = color,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AdminAppsListView(
    apps: List<AppItem>,
    onEdit: (AppItem) -> Unit,
    onDelete: (String) -> Unit,
    onTogglePublish: (String, Boolean) -> Unit,
    onAddNew: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = apps.filter {
        it.name.contains(search, ignoreCase = true) ||
        it.developer.contains(search, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AppSearchBar(
                    query = search,
                    onQueryChange = { search = it },
                    placeholder = "بحث في المحتوى..."
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onAddNew,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد تطبيقات مضافة بعد. اضغط (+) أو زر الرفع لإضافة تطبيق حقيقي عبر ملف APK.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(filtered) { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(app = app, modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)))

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${if (app.isGame) "لعبة" else "تطبيق"} • ${app.category} • ${app.version}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (app.isPublished) "حالة النشر: منشور علناً" else "حالة النشر: مخفي",
                                    color = if (app.isPublished) BrandGreen else BrandRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Toggle Publish
                            IconButton(onClick = { onTogglePublish(app.id, !app.isPublished) }) {
                                Icon(
                                    imageVector = if (app.isPublished) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Publish Toggle",
                                    tint = if (app.isPublished) BrandGreen else TextMuted
                                )
                            }

                            // Edit
                            IconButton(onClick = { onEdit(app) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = BrandBlue)
                            }

                            // Delete
                            IconButton(onClick = { onDelete(app.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = BrandRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesListView(
    categories: List<CategoryItem>,
    onDelete: (CategoryItem) -> Unit,
    onAddNew: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAddNew,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة تصنيف جديد", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            items(categories) { cat ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = cat.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${if (cat.isGameCategory) "ألعاب" else "تطبيقات"} • ${cat.iconName}", color = TextMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDelete(cat) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = BrandRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCoAdminsListView(
    coAdmins: List<AdminMember>,
    onAddAdmin: () -> Unit,
    onRemoveAdmin: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAddAdmin,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.GroupAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة مشرف جديد (Add Co-Admin)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            items(coAdmins) { admin ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (admin.role == "Primary Admin") BrandGreen else BrandBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = admin.displayName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = admin.email, color = TextMuted, fontSize = 12.sp)
                                Text(
                                    text = "الدور: ${admin.role} • الصلاحيات: رفع ونشر وحذف",
                                    color = BrandBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (admin.role != "Primary Admin") {
                            IconButton(onClick = { onRemoveAdmin(admin.email) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = BrandRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RealApkUploadDialog(
    app: AppItem,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSave: (AppItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(app.name) }
    var packageName by remember { mutableStateOf(app.packageName) }
    var developer by remember { mutableStateOf(app.developer.ifBlank { "App Store Plus Developer" }) }
    var category by remember { mutableStateOf(app.category.ifBlank { categories.firstOrNull()?.title ?: "أدوات" }) }
    var version by remember { mutableStateOf(app.version) }
    var versionCode by remember { mutableIntStateOf(app.versionCode) }
    var fileSize by remember { mutableStateOf(app.fileSize) }
    var minSdk by remember { mutableIntStateOf(app.minSdk) }
    var targetSdk by remember { mutableIntStateOf(app.targetSdk) }
    var abi by remember { mutableStateOf(app.abi) }
    var iconUrl by remember { mutableStateOf(app.iconUrl) }
    var description by remember { mutableStateOf(app.description) }
    var modInfo by remember { mutableStateOf(app.modInfo) }
    var isGame by remember { mutableStateOf(app.isGame) }
    var isFeatured by remember { mutableStateOf(app.isFeatured) }
    var isMostDownloaded by remember { mutableStateOf(app.isMostDownloaded) }
    var isPublished by remember { mutableStateOf(app.isPublished) }
    var isOpenSource by remember { mutableStateOf(app.isOpenSource) }
    var rootCompatibility by remember { mutableStateOf(app.rootCompatibility) }
    var uploadedLocalPath by remember { mutableStateOf(app.uploadedApkLocalPath) }

    var isExtractingApk by remember { mutableStateOf(false) }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    // Real APK File Picker
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isExtractingApk = true
            scope.launch {
                val result = ApkParser.copyAndExtractApk(context, uri)
                isExtractingApk = false
                result.onSuccess { extracted ->
                    name = extracted.appName
                    packageName = extracted.packageName
                    version = extracted.versionName
                    versionCode = extracted.versionCode
                    fileSize = extracted.fileSizeFormatted
                    minSdk = extracted.minSdk
                    targetSdk = extracted.targetSdk
                    abi = extracted.abi
                    uploadedLocalPath = extracted.savedApkFile.absolutePath
                    extracted.iconLocalPath?.let { iconUrl = "file://$it" }

                    Toast.makeText(context, "تم استخراج بيانات APK تلقائياً بنجاح!", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "خطأ في قراءة APK: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val rootCompatibilityOptions = listOf(
        "No root required" to "لا يتطلب روت (No root required)",
        "Root required" to "يتطلب روت (Root required)",
        "Root optional" to "روت اختياري (Root optional)",
        "Shizuku required" to "يتطلب Shizuku (Shizuku required)",
        "ADB required" to "يتطلب ADB (ADB required)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text(
                text = if (app.name.isBlank()) "رفع ونشر تطبيق/لعبة جديدة عبر APK" else "تعديل ${app.name}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // APK Selection Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (uploadedLocalPath.isNotBlank()) Color(0xFF064E3B) else BrandBlue)
                        .clickable { apkPickerLauncher.launch("*/*") }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isExtractingApk) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("جارٍ فك حزمة واستخراج بيانات APK...", color = Color.White, fontSize = 13.sp)
                        }
                    } else if (uploadedLocalPath.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تم استخراج بيانات ملف APK بنجاح! اضغط للتغيير", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FileOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اختر ملف APK من جهازك (رفع وتوليد تلقائي)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-extracted metadata banner
                if (uploadedLocalPath.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("معلومات مستخرجة تلقائياً من APK:", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الحزمة: $packageName", color = TextSecondary, fontSize = 11.sp)
                            Text("الإصدار: $version (كود: $versionCode) • الحجم: $fileSize", color = TextSecondary, fontSize = 11.sp)
                            Text("المعمارية: $abi • Min SDK: $minSdk • Target: $targetSdk", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم التطبيق أو اللعبة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Developer
                OutlinedTextField(
                    value = developer,
                    onValueChange = { developer = it },
                    label = { Text("المطور (Developer)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownOpen,
                    onExpandedChange = { isCategoryDropdownOpen = !isCategoryDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("التصنيف (Category)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownOpen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownOpen,
                        onDismissRequest = { isCategoryDropdownOpen = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.title) },
                                onClick = {
                                    category = cat.title
                                    isGame = cat.isGameCategory
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (Description)") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mod Information
                OutlinedTextField(
                    value = modInfo,
                    onValueChange = { modInfo = it },
                    label = { Text("معلومات التعديل / الميزات (Mod Info)") },
                    placeholder = { Text("مثال: ميزات إضافية مفتوحة، بدون إعلانات...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Root & Advanced Compatibility
                Text("توافق الروت والميزات المتقدمة:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                rootCompatibilityOptions.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rootCompatibility = key }
                            .padding(vertical = 4.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = rootCompatibility == key,
                            onClick = { rootCompatibility = key }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, color = if (rootCompatibility == key) BrandBlue else TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مفتوح المصدر (Open Source)", color = TextPrimary)
                    Switch(checked = isOpenSource, onCheckedChange = { isOpenSource = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("هل هو لعبة؟", color = TextPrimary)
                    Switch(checked = isGame, onCheckedChange = { isGame = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مميز في الصفحة الرئيسية", color = TextPrimary)
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("نشر علني في المتجر", color = TextPrimary)
                    Switch(checked = isPublished, onCheckedChange = { isPublished = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "يرجى إدخال اسم التطبيق أو رفع ملف APK", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSave(
                        app.copy(
                            name = name,
                            packageName = packageName,
                            developer = developer,
                            category = category,
                            version = version,
                            versionCode = versionCode,
                            fileSize = fileSize,
                            description = description,
                            apkDownloadUrl = if (uploadedLocalPath.isNotBlank()) "file://$uploadedLocalPath" else app.apkDownloadUrl,
                            uploadedApkLocalPath = uploadedLocalPath,
                            isGame = isGame,
                            isFeatured = isFeatured,
                            isMostDownloaded = isMostDownloaded,
                            isPublished = isPublished,
                            isOpenSource = isOpenSource,
                            rootCompatibility = rootCompatibility,
                            modInfo = modInfo,
                            minSdk = minSdk,
                            targetSdk = targetSdk,
                            abi = abi,
                            iconUrl = iconUrl
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("حفظ ونشر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (CategoryItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var iconName by remember { mutableStateOf("apps") }
    var isGameCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("إضافة تصنيف جديد", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم التصنيف") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تصنيف خاص بالألعاب", color = TextPrimary)
                    Switch(checked = isGameCat, onCheckedChange = { isGameCat = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            CategoryItem(
                                id = "cat_${UUID.randomUUID().toString().take(6)}",
                                title = title,
                                iconName = iconName,
                                colorHex = "#3B82F6",
                                isGameCategory = isGameCat
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

@Composable
private fun AddCoAdminDialog(
    onDismiss: () -> Unit,
    onSave: (AdminMember) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("إضافة مشرف جديد للمتجر", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشرف") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onSave(
                            AdminMember(
                                email = email.trim(),
                                displayName = name.ifBlank { email.substringBefore("@") },
                                role = "Co-Admin",
                                canPublish = true,
                                canDelete = true,
                                canManageCategories = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("منح الصلاحية", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
