package com.storytold.pdfcraft.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytold.pdfcraft.core.RecentFileItem
import com.storytold.pdfcraft.core.RecentFilesManager
import com.storytold.pdfcraft.ui.theme.AcrobatActionBlue
import com.storytold.pdfcraft.ui.theme.AcrobatRed

enum class AcrobatTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    FILES("Files", Icons.Default.Folder),
    TOOLS("Tools", Icons.Default.Apps)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPdfSelected: (Uri) -> Unit,
    onNavigateToTools: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableStateOf(AcrobatTab.HOME) }
    var selectedHomeFilter by remember { mutableIntStateOf(0) } // 0 = Recent, 1 = Starred
    var searchQuery by remember { mutableStateOf("") }

    val recentFiles = remember { mutableStateListOf<RecentFileItem>() }

    fun refreshFiles() {
        recentFiles.clear()
        recentFiles.addAll(RecentFilesManager.getRecentFiles(context))
    }

    LaunchedEffect(Unit) {
        refreshFiles()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onPdfSelected(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AcrobatRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PdfCraft",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Offline Acrobat Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = AcrobatRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Adobe Acrobat Mobile 3-Tab Bottom Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AcrobatTab.values().forEach { tab ->
                    val isSelected = selectedBottomTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedBottomTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AcrobatRed,
                            selectedTextColor = AcrobatRed,
                            indicatorColor = Color(0xFFFFEBEE),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Adobe Acrobat Iconic Blue FAB for Instant Document Selection
            FloatingActionButton(
                onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                shape = CircleShape,
                containerColor = AcrobatActionBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Open PDF",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedBottomTab) {
                AcrobatTab.HOME -> {
                    HomeScreenContent(
                        recentFiles = recentFiles,
                        selectedFilter = selectedHomeFilter,
                        onFilterChanged = { selectedHomeFilter = it },
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { searchQuery = it },
                        onFileClick = { item -> onPdfSelected(item.uri) },
                        onToggleStar = { item ->
                            RecentFilesManager.toggleStarred(context, item.uriString)
                            refreshFiles()
                        },
                        onDeleteRecent = { item ->
                            RecentFilesManager.removeFile(context, item.uriString)
                            refreshFiles()
                        },
                        onOpenStorage = { filePickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                AcrobatTab.FILES -> {
                    FilesScreenContent(
                        recentFiles = recentFiles,
                        onFileClick = { item -> onPdfSelected(item.uri) },
                        onBrowseSystem = { filePickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                AcrobatTab.TOOLS -> {
                    ToolsCatalogContent(
                        onSelectTool = { toolId -> onNavigateToTools(toolId) },
                        onOpenFile = { filePickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenContent(
    recentFiles: List<RecentFileItem>,
    selectedFilter: Int,
    onFilterChanged: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFileClick: (RecentFileItem) -> Unit,
    onToggleStar: (RecentFileItem) -> Unit,
    onDeleteRecent: (RecentFileItem) -> Unit,
    onOpenStorage: () -> Unit
) {
    val filteredList = recentFiles.filter { item ->
        val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (selectedFilter == 1) item.isStarred else true
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search files...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Acrobat Segmented Tabs: Recent | Starred
        TabRow(
            selectedTabIndex = selectedFilter,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AcrobatRed,
            indicator = { tabPositions ->
                if (selectedFilter < tabPositions.size) {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedFilter])
                            .height(3.dp)
                            .background(AcrobatRed)
                    )
                }
            }
        ) {
            Tab(
                selected = selectedFilter == 0,
                onClick = { onFilterChanged(0) },
                text = { Text("Recent", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedFilter == 1,
                onClick = { onFilterChanged(1) },
                text = { Text("Starred", fontWeight = FontWeight.SemiBold) }
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedFilter == 1) Icons.Default.Star else Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = AcrobatRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = if (selectedFilter == 1) "No Starred Files" else "No Recent PDFs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedFilter == 1) "Star your favorite documents for quick access." else "Open a PDF from your device storage to begin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedCard(
                        onClick = onOpenStorage,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = AcrobatActionBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open PDF File", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filteredList) { item ->
                    RecentPdfItemRow(
                        item = item,
                        onClick = { onFileClick(item) },
                        onToggleStar = { onToggleStar(item) },
                        onDelete = { onDeleteRecent(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPdfItemRow(
    item: RecentFileItem,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Acrobat Document Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = AcrobatRed,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.formattedSize(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.formattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Star Favorite Toggle
        IconButton(onClick = onToggleStar) {
            Icon(
                imageVector = if (item.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Star",
                tint = if (item.isStarred) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        // 3-Dot Overflow Menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Open") },
                    leadingIcon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (item.isStarred) "Unstar" else "Star") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onToggleStar()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Remove from Recents") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun FilesScreenContent(
    recentFiles: List<RecentFileItem>,
    onFileClick: (RecentFileItem) -> Unit,
    onBrowseSystem: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                onClick = onBrowseSystem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AcrobatActionBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Browse Device Files",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Internal Storage, Downloads, SD Card",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (recentFiles.isNotEmpty()) {
            item {
                Text(
                    text = "On This Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(recentFiles) { item ->
                RecentPdfItemRow(
                    item = item,
                    onClick = { onFileClick(item) },
                    onToggleStar = {},
                    onDelete = {}
                )
            }
        }
    }
}

@Composable
private fun ToolsCatalogContent(
    onSelectTool: (String) -> Unit,
    onOpenFile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Edit & Convert",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AcrobatToolTile(
                    title = "Edit & Sign",
                    subtitle = "Fill, text & signature",
                    icon = Icons.Default.Draw,
                    badgeColor = AcrobatActionBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFile
                )
                AcrobatToolTile(
                    title = "Organize Pages",
                    subtitle = "Rotate, delete & order",
                    icon = Icons.Default.ViewModule,
                    badgeColor = Color(0xFF7E57C2),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTool("organize") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AcrobatToolTile(
                    title = "Merge PDFs",
                    subtitle = "Combine files",
                    icon = Icons.Default.CallMerge,
                    badgeColor = Color(0xFF00897B),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTool("merge") }
                )
                AcrobatToolTile(
                    title = "Split PDF",
                    subtitle = "Extract page ranges",
                    icon = Icons.Default.CallSplit,
                    badgeColor = Color(0xFFE65100),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTool("split") }
                )
            }
        }

        item {
            Text(
                text = "Security & Optimization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AcrobatToolTile(
                    title = "Protect PDF",
                    subtitle = "AES-256 Encryption",
                    icon = Icons.Default.Lock,
                    badgeColor = AcrobatRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTool("protect") }
                )
                AcrobatToolTile(
                    title = "Compress PDF",
                    subtitle = "Reduce document size",
                    icon = Icons.Default.Compress,
                    badgeColor = Color(0xFF3949AB),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTool("compress") }
                )
            }
        }
    }
}

@Composable
private fun AcrobatToolTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
