package com.storytold.pdfcraft.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.storytold.pdfcraft.core.PdfDocument
import com.storytold.pdfcraft.ui.theme.AcrobatActionBlue
import com.storytold.pdfcraft.ui.theme.AcrobatRed
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeScreen(
    document: PdfDocument,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pageCount by remember { mutableStateOf(document.pageCount) }
    val selectedPages = remember { mutableStateMapOf<Int, Boolean>() }
    val thumbnails = remember { mutableStateMapOf<Int, Bitmap?>() }

    fun refreshThumbnails() {
        scope.launch {
            for (i in 0 until pageCount) {
                thumbnails[i] = document.renderThumbnail(i, 280)
            }
        }
    }

    LaunchedEffect(pageCount) {
        refreshThumbnails()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val countSelected = selectedPages.values.count { it }
                    Text(
                        text = if (countSelected > 0) "$countSelected of $pageCount Selected" else "Organize Pages ($pageCount)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val anySelected = selectedPages.values.any { it }
                    if (anySelected) {
                        Text(
                            text = "Deselect",
                            modifier = Modifier
                                .clickable { selectedPages.clear() }
                                .padding(end = 16.dp),
                            color = AcrobatActionBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Select All",
                            modifier = Modifier
                                .clickable {
                                    for (i in 0 until pageCount) selectedPages[i] = true
                                }
                                .padding(end = 16.dp),
                            color = AcrobatActionBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            val selectedIndices = selectedPages.filter { it.value }.keys.map { it.toInt() }.sorted().toIntArray()
            val hasSelection = selectedIndices.isNotEmpty()

            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                actions = {
                    // Rotate Clockwise
                    IconButton(
                        onClick = {
                            scope.launch {
                                document.rotatePages(selectedIndices, 90)
                                refreshThumbnails()
                                Toast.makeText(context, "Rotated 90° Clockwise", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasSelection
                    ) {
                        Icon(
                            imageVector = Icons.Default.Rotate90DegreesCw,
                            contentDescription = "Rotate CW",
                            tint = if (hasSelection) AcrobatActionBlue else Color.Gray
                        )
                    }

                    // Rotate Counter-Clockwise
                    IconButton(
                        onClick = {
                            scope.launch {
                                document.rotatePages(selectedIndices, 270)
                                refreshThumbnails()
                                Toast.makeText(context, "Rotated 90° CCW", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasSelection
                    ) {
                        Icon(
                            imageVector = Icons.Default.Rotate90DegreesCcw,
                            contentDescription = "Rotate CCW",
                            tint = if (hasSelection) AcrobatActionBlue else Color.Gray
                        )
                    }

                    // Move Page Left / Earlier
                    IconButton(
                        onClick = {
                            if (selectedIndices.size == 1 && selectedIndices[0] > 0) {
                                val from = selectedIndices[0]
                                val to = from - 1
                                scope.launch {
                                    document.movePage(from, to)
                                    selectedPages.clear()
                                    selectedPages[to] = true
                                    refreshThumbnails()
                                }
                            }
                        },
                        enabled = selectedIndices.size == 1 && selectedIndices[0] > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Move Left",
                            tint = if (selectedIndices.size == 1 && selectedIndices[0] > 0) AcrobatActionBlue else Color.Gray
                        )
                    }

                    // Move Page Right / Later
                    IconButton(
                        onClick = {
                            if (selectedIndices.size == 1 && selectedIndices[0] < pageCount - 1) {
                                val from = selectedIndices[0]
                                val to = from + 1
                                scope.launch {
                                    document.movePage(from, to)
                                    selectedPages.clear()
                                    selectedPages[to] = true
                                    refreshThumbnails()
                                }
                            }
                        },
                        enabled = selectedIndices.size == 1 && selectedIndices[0] < pageCount - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Move Right",
                            tint = if (selectedIndices.size == 1 && selectedIndices[0] < pageCount - 1) AcrobatActionBlue else Color.Gray
                        )
                    }

                    // Delete Selected Pages
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (pageCount > selectedIndices.size) {
                                    document.deletePages(selectedIndices)
                                    selectedPages.clear()
                                    pageCount = document.pageCount
                                    refreshThumbnails()
                                    Toast.makeText(context, "Pages deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "PDF must contain at least one page", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = hasSelection && pageCount > selectedIndices.size
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Pages",
                            tint = if (hasSelection && pageCount > selectedIndices.size) AcrobatRed else Color.Gray
                        )
                    }

                    // Extract Selected Pages into New PDF
                    IconButton(
                        onClick = {
                            scope.launch {
                                val bytes = document.extractPages(selectedIndices)
                                if (bytes != null) {
                                    shareExtractedPdf(context, bytes, document.name)
                                }
                            }
                        },
                        enabled = hasSelection
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Extract Pages",
                            tint = if (hasSelection) AcrobatActionBlue else Color.Gray
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val savedBytes = document.save()
                                if (savedBytes != null) {
                                    Toast.makeText(context, "Saved changes successfully", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        },
                        containerColor = AcrobatActionBlue,
                        contentColor = Color.White
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save Changes")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(pageCount) { index ->
                val isSelected = selectedPages[index] == true
                val thumbnail = thumbnails[index]

                Box(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) AcrobatActionBlue else Color(0xFFD6D6D6),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color.White)
                        .clickable {
                            selectedPages[index] = !isSelected
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(
                            color = AcrobatRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Page number badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .background(
                                color = Color(0xDD212121),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Selection Checkmark Badge
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AcrobatActionBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shareExtractedPdf(context: Context, bytes: ByteArray, originalName: String) {
    try {
        val baseName = originalName.substringBeforeLast(".")
        val extractFile = File(context.cacheDir, "${baseName}_extracted.pdf")
        extractFile.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            extractFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Extracted PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share extracted PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
