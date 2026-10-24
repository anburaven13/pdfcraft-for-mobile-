package com.storytold.pdfcraft.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.storytold.pdfcraft.core.PdfDocument
import com.storytold.pdfcraft.ui.components.AcrobatCommentBar
import com.storytold.pdfcraft.ui.components.AcrobatFillSignBar
import com.storytold.pdfcraft.ui.components.AcrobatSpeedDial
import com.storytold.pdfcraft.ui.components.CommentTool
import com.storytold.pdfcraft.ui.components.FillSignTool
import com.storytold.pdfcraft.ui.components.PageCommentNote
import com.storytold.pdfcraft.ui.components.PageIndicatorPill
import com.storytold.pdfcraft.ui.components.PageStamp
import com.storytold.pdfcraft.ui.components.PdfPageView
import com.storytold.pdfcraft.ui.components.SignaturePadDialog
import com.storytold.pdfcraft.ui.theme.AcrobatActionBlue
import com.storytold.pdfcraft.ui.theme.AcrobatRed
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    document: PdfDocument,
    onBack: () -> Unit,
    onNavigateToOrganize: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val totalPages = document.pageCount

    // View state
    var isContinuousScroll by remember { mutableStateOf(true) }
    var singlePageIndex by remember { mutableIntStateOf(0) }
    var nightMode by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var areBarsVisible by remember { mutableStateOf(true) }

    // Lazy list state for continuous vertical scroll
    val listState = rememberLazyListState()
    val currentScrollPage by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        }
    }
    val activeDisplayPage = if (isContinuousScroll) currentScrollPage else singlePageIndex

    // Acrobat Speed Dial & Tools State
    var isSpeedDialExpanded by remember { mutableStateOf(false) }
    var activeFillSignTool by remember { mutableStateOf(FillSignTool.NONE) }
    var activeCommentTool by remember { mutableStateOf(CommentTool.NONE) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var pendingSignatureBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Annotation Data Maps (per-page)
    val pageStamps = remember { mutableStateListOf<PageStamp>() }
    val pageNotes = remember { mutableStateListOf<PageCommentNote>() }

    // In-Document Search State
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchMatchPages = remember { mutableStateListOf<Int>() }
    var currentMatchIndex by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }

    // Text & Note input dialogs
    var showTextInputDialog by remember { mutableStateOf(false) }
    var pendingTextOffset by remember { mutableStateOf(Offset.Zero) }
    var customTextValue by remember { mutableStateOf("") }

    var showNoteInputDialog by remember { mutableStateOf(false) }
    var pendingNoteOffset by remember { mutableStateOf(Offset.Zero) }
    var noteInputValue by remember { mutableStateOf("") }

    // Full Extracted Text Dialog
    var showExtractedTextDialog by remember { mutableStateOf(false) }
    var extractedTextContent by remember { mutableStateOf("") }

    // Search Execution
    fun executeSearch(query: String) {
        if (query.isBlank()) {
            searchMatchPages.clear()
            currentMatchIndex = 0
            return
        }
        scope.launch {
            isSearching = true
            searchMatchPages.clear()
            for (p in 0 until totalPages) {
                val pageText = document.getPageText(p)
                if (pageText.contains(query, ignoreCase = true)) {
                    searchMatchPages.add(p)
                }
            }
            isSearching = false
            if (searchMatchPages.isNotEmpty()) {
                currentMatchIndex = 0
                val targetPage = searchMatchPages[0]
                if (isContinuousScroll) {
                    listState.animateScrollToItem(targetPage)
                } else {
                    singlePageIndex = targetPage
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = areBarsVisible || isSearchActive || activeCommentTool != CommentTool.NONE || activeFillSignTool != FillSignTool.NONE,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                if (isSearchActive) {
                    // Acrobat In-Document Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                                searchMatchPages.clear()
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Search")
                            }
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    executeSearch(it)
                                },
                                placeholder = { Text("Find in document...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { executeSearch(searchQuery) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Match counter
                            Text(
                                text = if (searchMatchPages.isEmpty()) {
                                    if (searchQuery.isBlank()) "" else "0 of 0"
                                } else {
                                    "${currentMatchIndex + 1} of ${searchMatchPages.size}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            // Previous Match Button
                            IconButton(
                                onClick = {
                                    if (searchMatchPages.isNotEmpty()) {
                                        val newIdx = if (currentMatchIndex > 0) currentMatchIndex - 1 else searchMatchPages.size - 1
                                        currentMatchIndex = newIdx
                                        val targetPage = searchMatchPages[newIdx]
                                        scope.launch {
                                            if (isContinuousScroll) listState.animateScrollToItem(targetPage) else singlePageIndex = targetPage
                                        }
                                    }
                                },
                                enabled = searchMatchPages.isNotEmpty()
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match")
                            }
                            // Next Match Button
                            IconButton(
                                onClick = {
                                    if (searchMatchPages.isNotEmpty()) {
                                        val newIdx = if (currentMatchIndex < searchMatchPages.size - 1) currentMatchIndex + 1 else 0
                                        currentMatchIndex = newIdx
                                        val targetPage = searchMatchPages[newIdx]
                                        scope.launch {
                                            if (isContinuousScroll) listState.animateScrollToItem(targetPage) else singlePageIndex = targetPage
                                        }
                                    }
                                },
                                enabled = searchMatchPages.isNotEmpty()
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Next Match")
                            }
                        }
                    }
                } else if (activeCommentTool != CommentTool.NONE) {
                    // Acrobat Comment Mode Top Bar
                    TopAppBar(
                        title = { Text("Comment", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = { activeCommentTool = CommentTool.NONE }) {
                                Text("Done", color = AcrobatActionBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (pageNotes.isNotEmpty()) pageNotes.removeLastOrNull()
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                            }
                            IconButton(onClick = {
                                pageNotes.clear()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Comments")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                } else if (activeFillSignTool != FillSignTool.NONE) {
                    // Acrobat Fill & Sign Mode Top Bar
                    TopAppBar(
                        title = { Text("Fill & Sign", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = { activeFillSignTool = FillSignTool.NONE }) {
                                Text("Done", color = AcrobatActionBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (pageStamps.isNotEmpty()) pageStamps.removeLastOrNull()
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                            }
                            IconButton(onClick = {
                                pageStamps.clear()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Stamps")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                } else {
                    // Standard Acrobat Top Bar
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = document.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Page ${activeDisplayPage + 1} of $totalPages",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcrobatActionBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            // In-Document Search Trigger
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                            // Toggle Continuous Scroll vs Single Page
                            IconButton(onClick = { isContinuousScroll = !isContinuousScroll }) {
                                Icon(
                                    imageVector = if (isContinuousScroll) Icons.Default.ViewStream else Icons.Default.ViewCarousel,
                                    contentDescription = if (isContinuousScroll) "Continuous Scroll" else "Single Page"
                                )
                            }
                            // Night Mode Reading
                            IconButton(onClick = { nightMode = !nightMode }) {
                                Icon(
                                    imageVector = if (nightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Night Mode"
                                )
                            }
                            // Organize Pages Shortcut
                            IconButton(onClick = onNavigateToOrganize) {
                                Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = "Organize")
                            }
                            // Share PDF
                            IconButton(onClick = { sharePdf(context, document) }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            }
                            // Overflow Menu
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Extract All Page Text") },
                                        leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            scope.launch {
                                                extractedTextContent = document.getPageText(activeDisplayPage)
                                                showExtractedTextDialog = true
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Document Info") },
                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            val dims = document.getPageDimensions(activeDisplayPage)
                                            Toast.makeText(
                                                context,
                                                "Document: ${document.name}\nPages: $totalPages\nCurrent Size: ${dims.first.toInt()} x ${dims.second.toInt()} pt",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = areBarsVisible || activeCommentTool != CommentTool.NONE || activeFillSignTool != FillSignTool.NONE,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (activeFillSignTool != FillSignTool.NONE) {
                    AcrobatFillSignBar(
                        activeTool = activeFillSignTool,
                        onSelectTool = { tool ->
                            activeFillSignTool = tool
                            if (tool == FillSignTool.SIGNATURE && pendingSignatureBitmap == null) {
                                showSignatureDialog = true
                            }
                        },
                        onDone = {
                            activeFillSignTool = FillSignTool.NONE
                            Toast.makeText(context, "Annotations saved", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else if (activeCommentTool != CommentTool.NONE) {
                    AcrobatCommentBar(
                        activeTool = activeCommentTool,
                        onSelectTool = { tool -> activeCommentTool = tool },
                        onDone = {
                            activeCommentTool = CommentTool.NONE
                            Toast.makeText(context, "Comments saved", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else if (!isContinuousScroll) {
                    // Single Page Navigation Bar
                    Surface(
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (singlePageIndex > 0) singlePageIndex-- },
                                enabled = singlePageIndex > 0
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                            }
                            Text(
                                text = "${singlePageIndex + 1} / $totalPages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { if (singlePageIndex < totalPages - 1) singlePageIndex++ },
                                enabled = singlePageIndex < totalPages - 1
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isContinuousScroll) {
                // Continuous Vertical Scrolling (Acrobat Default)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (nightMode) Color(0xFF181818) else Color(0xFFE5E5E5)),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(count = totalPages) { pageIdx ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(580.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PdfPageView(
                                document = document,
                                pageIndex = pageIdx,
                                nightMode = nightMode,
                                activeFillSignTool = activeFillSignTool,
                                activeCommentTool = activeCommentTool,
                                onBackgroundTap = {
                                    if (activeFillSignTool == FillSignTool.NONE && activeCommentTool == CommentTool.NONE) {
                                        areBarsVisible = !areBarsVisible
                                    }
                                },
                                onFillSignTap = { offset ->
                                    handleFillSignTap(
                                        tool = activeFillSignTool,
                                        offset = offset,
                                        pendingSignature = pendingSignatureBitmap,
                                        onOpenSignatureDialog = { showSignatureDialog = true },
                                        onOpenTextPrompt = {
                                            pendingTextOffset = offset
                                            showTextInputDialog = true
                                        },
                                        onAddStamp = { stamp -> pageStamps.add(stamp) }
                                    )
                                },
                                onCommentTap = { offset ->
                                    pendingNoteOffset = offset
                                    showNoteInputDialog = true
                                },
                                stamps = pageStamps,
                                notes = pageNotes,
                                onDeleteStamp = { id -> pageStamps.removeAll { it.id == id } },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                // Single Page Canvas
                PdfPageView(
                    document = document,
                    pageIndex = singlePageIndex,
                    nightMode = nightMode,
                    activeFillSignTool = activeFillSignTool,
                    activeCommentTool = activeCommentTool,
                    onBackgroundTap = {
                        if (activeFillSignTool == FillSignTool.NONE && activeCommentTool == CommentTool.NONE) {
                            areBarsVisible = !areBarsVisible
                        }
                    },
                    onFillSignTap = { offset ->
                        handleFillSignTap(
                            tool = activeFillSignTool,
                            offset = offset,
                            pendingSignature = pendingSignatureBitmap,
                            onOpenSignatureDialog = { showSignatureDialog = true },
                            onOpenTextPrompt = {
                                pendingTextOffset = offset
                                showTextInputDialog = true
                            },
                            onAddStamp = { stamp -> pageStamps.add(stamp) }
                        )
                    },
                    onCommentTap = { offset ->
                        pendingNoteOffset = offset
                        showNoteInputDialog = true
                    },
                    stamps = pageStamps,
                    notes = pageNotes,
                    onDeleteStamp = { id -> pageStamps.removeAll { it.id == id } },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Acrobat Floating Page Indicator Scrubber
            PageIndicatorPill(
                currentPage = activeDisplayPage,
                totalPages = totalPages,
                visible = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            )

            // Acrobat Speed Dial Floating Action Button
            if (activeFillSignTool == FillSignTool.NONE && activeCommentTool == CommentTool.NONE) {
                AnimatedVisibility(
                    visible = areBarsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 20.dp)
                ) {
                    AcrobatSpeedDial(
                        isExpanded = isSpeedDialExpanded,
                        onToggleExpand = { isSpeedDialExpanded = !isSpeedDialExpanded },
                        onFillAndSign = {
                            activeFillSignTool = FillSignTool.TEXT
                        },
                        onComment = {
                            activeCommentTool = CommentTool.HIGHLIGHT
                        },
                        onOrganize = onNavigateToOrganize,
                        onExtractText = {
                            scope.launch {
                                extractedTextContent = document.getPageText(activeDisplayPage)
                                showExtractedTextDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    // Signature Drawing Pad Dialog
    if (showSignatureDialog) {
        SignaturePadDialog(
            onDismiss = { showSignatureDialog = false },
            onSignatureConfirmed = { bmp ->
                pendingSignatureBitmap = bmp
                showSignatureDialog = false
                activeFillSignTool = FillSignTool.SIGNATURE
                Toast.makeText(context, "Tap anywhere on page to stamp signature", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Text Input Stamp Prompt
    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = { Text("Insert Text") },
            text = {
                OutlinedTextField(
                    value = customTextValue,
                    onValueChange = { customTextValue = it },
                    label = { Text("Text to place") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customTextValue.isNotBlank()) {
                        pageStamps.add(
                            PageStamp(
                                type = FillSignTool.TEXT,
                                x = pendingTextOffset.x,
                                y = pendingTextOffset.y,
                                text = customTextValue
                            )
                        )
                        customTextValue = ""
                    }
                    showTextInputDialog = false
                }) {
                    Text("Insert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sticky Note Input Prompt
    if (showNoteInputDialog) {
        AlertDialog(
            onDismissRequest = { showNoteInputDialog = false },
            title = { Text("Add Sticky Note") },
            text = {
                OutlinedTextField(
                    value = noteInputValue,
                    onValueChange = { noteInputValue = it },
                    label = { Text("Comment note") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteInputValue.isNotBlank()) {
                        pageNotes.add(
                            PageCommentNote(
                                x = pendingNoteOffset.x,
                                y = pendingNoteOffset.y,
                                text = noteInputValue
                            )
                        )
                        noteInputValue = ""
                    }
                    showNoteInputDialog = false
                }) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Extracted Text Dialog
    if (showExtractedTextDialog) {
        AlertDialog(
            onDismissRequest = { showExtractedTextDialog = false },
            title = { Text("Extracted Text (Page ${activeDisplayPage + 1})") },
            text = {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (extractedTextContent.isNotBlank()) extractedTextContent else "No selectable text on this page.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExtractedTextDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun handleFillSignTap(
    tool: FillSignTool,
    offset: Offset,
    pendingSignature: Bitmap?,
    onOpenSignatureDialog: () -> Unit,
    onOpenTextPrompt: () -> Unit,
    onAddStamp: (PageStamp) -> Unit
) {
    when (tool) {
        FillSignTool.CHECKMARK -> {
            onAddStamp(PageStamp(type = FillSignTool.CHECKMARK, x = offset.x - 20f, y = offset.y - 20f))
        }
        FillSignTool.CROSS -> {
            onAddStamp(PageStamp(type = FillSignTool.CROSS, x = offset.x - 20f, y = offset.y - 20f))
        }
        FillSignTool.DATE -> {
            val dateStr = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
            onAddStamp(PageStamp(type = FillSignTool.DATE, x = offset.x, y = offset.y, text = dateStr))
        }
        FillSignTool.TEXT -> {
            onOpenTextPrompt()
        }
        FillSignTool.SIGNATURE -> {
            if (pendingSignature != null) {
                onAddStamp(PageStamp(type = FillSignTool.SIGNATURE, x = offset.x - 60f, y = offset.y - 30f, bitmap = pendingSignature))
            } else {
                onOpenSignatureDialog()
            }
        }
        FillSignTool.NONE -> {}
    }
}

private fun sharePdf(context: Context, document: PdfDocument) {
    try {
        val cacheFile = File(context.cacheDir, document.name.ifBlank { "document.pdf" })
        cacheFile.writeBytes(document.initialBytes)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            cacheFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
