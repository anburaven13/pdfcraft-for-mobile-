package com.storytold.pdfcraft

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import com.storytold.pdfcraft.core.PdfDocument
import com.storytold.pdfcraft.ui.components.PasswordDialog
import com.storytold.pdfcraft.ui.screens.HomeScreen
import com.storytold.pdfcraft.ui.screens.OrganizeScreen
import com.storytold.pdfcraft.ui.screens.ToolsScreen
import com.storytold.pdfcraft.ui.screens.ViewerScreen
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object Viewer : Screen()
    data object Organize : Screen()
    data class Tools(val initialTool: String) : Screen()
}

@Composable
fun PdfCraftApp(
    initialPdfUri: Uri? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var activeDocument by remember { mutableStateOf<PdfDocument?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Password prompt state
    var pendingPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingPdfName by remember { mutableStateOf("document.pdf") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    fun loadPdfFromUri(uri: Uri, password: String? = null) {
        scope.launch {
            isLoading = true
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    Toast.makeText(context, "Could not read file", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val docName = DocumentFile.fromSingleUri(context, uri)?.name ?: "document.pdf"
                val doc = PdfDocument.open(bytes, name = docName, password = password)
                if (doc != null) {
                    activeDocument?.close()
                    activeDocument = doc
                    currentScreen = Screen.Viewer
                } else {
                    // Might be password protected
                    pendingPdfBytes = bytes
                    pendingPdfName = docName
                    showPasswordDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(initialPdfUri) {
        if (initialPdfUri != null) {
            loadPdfFromUri(initialPdfUri)
        }
    }

    // Clean up document on exit
    DisposableEffect(Unit) {
        onDispose {
            activeDocument?.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onPdfSelected = { uri -> loadPdfFromUri(uri) },
                    onNavigateToTools = { tool -> currentScreen = Screen.Tools(tool) }
                )
            }
            is Screen.Viewer -> {
                val doc = activeDocument
                if (doc != null) {
                    ViewerScreen(
                        document = doc,
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToOrganize = { currentScreen = Screen.Organize }
                    )
                } else {
                    currentScreen = Screen.Home
                }
            }
            is Screen.Organize -> {
                val doc = activeDocument
                if (doc != null) {
                    OrganizeScreen(
                        document = doc,
                        onBack = { currentScreen = Screen.Viewer }
                    )
                } else {
                    currentScreen = Screen.Home
                }
            }
            is Screen.Tools -> {
                ToolsScreen(
                    initialTool = screen.initialTool,
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (showPasswordDialog) {
            PasswordDialog(
                onDismiss = {
                    showPasswordDialog = false
                    pendingPdfBytes = null
                },
                onConfirm = { pwd ->
                    showPasswordDialog = false
                    val bytes = pendingPdfBytes
                    if (bytes != null) {
                        scope.launch {
                            isLoading = true
                            val doc = PdfDocument.open(bytes, name = pendingPdfName, password = pwd)
                            isLoading = false
                            if (doc != null) {
                                activeDocument?.close()
                                activeDocument = doc
                                currentScreen = Screen.Viewer
                            } else {
                                Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}
