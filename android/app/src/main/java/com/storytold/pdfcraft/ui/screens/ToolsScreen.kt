package com.storytold.pdfcraft.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun ToolsScreen(
    initialTool: String = "merge",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val toolTabs = listOf("merge", "protect", "compress", "split")
    var activeTab by remember { mutableStateOf(if (initialTool in toolTabs) initialTool else "merge") }

    // Merge State
    val selectedMergeUris = remember { mutableStateListOf<Uri>() }
    var isMerging by remember { mutableStateOf(false) }

    val multiFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            selectedMergeUris.addAll(uris)
        }
    )

    // Protect State
    var protectUri by remember { mutableStateOf<Uri?>(null) }
    var userPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isEncrypting by remember { mutableStateOf(false) }

    val protectFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            protectUri = uri
        }
    )

    // Compress State
    var compressUri by remember { mutableStateOf<Uri?>(null) }
    var isCompressing by remember { mutableStateOf(false) }

    val compressFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            compressUri = uri
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeTab) {
                            "merge" -> "Merge PDF Files"
                            "protect" -> "Protect with Password"
                            "compress" -> "Compress PDF"
                            "split" -> "Split & Extract Pages"
                            else -> "PDF Workbench"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header Row
            val activeIndex = toolTabs.indexOf(activeTab).coerceAtLeast(0)
            TabRow(
                selectedTabIndex = activeIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AcrobatRed,
                indicator = { tabPositions ->
                    if (activeIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[activeIndex])
                                .height(3.dp)
                                .background(AcrobatRed)
                        )
                    }
                }
            ) {
                Tab(
                    selected = activeTab == "merge",
                    onClick = { activeTab = "merge" },
                    text = { Text("Merge", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == "protect",
                    onClick = { activeTab = "protect" },
                    text = { Text("Protect", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == "compress",
                    onClick = { activeTab = "compress" },
                    text = { Text("Compress", fontWeight = FontWeight.SemiBold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    "merge" -> {
                        Text(
                            text = "Combine multiple PDFs into a single continuous document.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { multiFilePicker.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AcrobatActionBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select PDF Files to Merge", color = AcrobatActionBlue, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(selectedMergeUris) { index, uri ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFEBEE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = AcrobatRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${index + 1}. ${uri.lastPathSegment ?: "Document"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { selectedMergeUris.removeAt(index) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isMerging = true
                                    try {
                                        val byteArrays = selectedMergeUris.mapNotNull { uri ->
                                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        }
                                        if (byteArrays.size >= 2) {
                                            val mergedBytes = PdfDocument.merge(byteArrays)
                                            if (mergedBytes != null) {
                                                shareOutputPdf(context, mergedBytes, "merged_document.pdf")
                                                Toast.makeText(context, "Successfully merged documents!", Toast.LENGTH_LONG).show()
                                                onBack()
                                            } else {
                                                Toast.makeText(context, "Failed to merge documents", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Please select at least 2 PDF files to merge", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        isMerging = false
                                    }
                                }
                            },
                            enabled = selectedMergeUris.size >= 2 && !isMerging,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AcrobatActionBlue)
                        ) {
                            Icon(imageVector = Icons.Default.CallMerge, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isMerging) "Merging..." else "Merge ${selectedMergeUris.size} Documents", fontWeight = FontWeight.Bold)
                        }
                    }

                    "protect" -> {
                        Text(
                            text = "Encrypt your PDF with standard AES-256 password protection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ElevatedCard(
                            onClick = { protectFilePicker.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                                        .background(AcrobatRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = protectUri?.lastPathSegment ?: "Choose PDF to Protect",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (protectUri != null) "Ready for encryption" else "Tap to browse file",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = userPassword,
                            onValueChange = { userPassword = it },
                            label = { Text("Set Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        val passwordsMatch = userPassword.isNotBlank() && userPassword == confirmPassword

                        Button(
                            onClick = {
                                scope.launch {
                                    isEncrypting = true
                                    try {
                                        val uri = protectUri ?: return@launch
                                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        if (bytes != null) {
                                            val doc = PdfDocument.open(bytes)
                                            if (doc != null) {
                                                val encryptedBytes = doc.encrypt(userPassword)
                                                doc.close()
                                                if (encryptedBytes != null) {
                                                    shareOutputPdf(context, encryptedBytes, "protected_${protectUri?.lastPathSegment ?: "doc.pdf"}")
                                                    Toast.makeText(context, "Encrypted PDF successfully!", Toast.LENGTH_LONG).show()
                                                    onBack()
                                                } else {
                                                    Toast.makeText(context, "Encryption failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } finally {
                                        isEncrypting = false
                                    }
                                }
                            },
                            enabled = protectUri != null && passwordsMatch && !isEncrypting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AcrobatRed)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEncrypting) "Encrypting..." else "Protect PDF with Password", fontWeight = FontWeight.Bold)
                        }
                    }

                    "compress" -> {
                        Text(
                            text = "Optimize PDF data streams and structure to reduce file size.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ElevatedCard(
                            onClick = { compressFilePicker.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                                        .background(Color(0xFF3949AB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Compress,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = compressUri?.lastPathSegment ?: "Choose PDF to Compress",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (compressUri != null) "Ready for optimization" else "Tap to browse file",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                scope.launch {
                                    isCompressing = true
                                    try {
                                        val uri = compressUri ?: return@launch
                                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        if (bytes != null) {
                                            val doc = PdfDocument.open(bytes)
                                            if (doc != null) {
                                                val savedBytes = doc.save()
                                                doc.close()
                                                if (savedBytes != null) {
                                                    shareOutputPdf(context, savedBytes, "compressed_${compressUri?.lastPathSegment ?: "doc.pdf"}")
                                                    Toast.makeText(context, "Compressed successfully (${savedBytes.size} bytes)!", Toast.LENGTH_LONG).show()
                                                    onBack()
                                                }
                                            }
                                        }
                                    } finally {
                                        isCompressing = false
                                    }
                                }
                            },
                            enabled = compressUri != null && !isCompressing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                        ) {
                            Icon(imageVector = Icons.Default.Compress, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isCompressing) "Compressing..." else "Compress & Optimize PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun shareOutputPdf(context: Context, bytes: ByteArray, fileName: String) {
    try {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
