package com.storytold.pdfcraft.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.storytold.pdfcraft.core.PdfDocument

/**
 * Interactive touch canvas for a single PDF page with smooth pinch-zoom and pan.
 */
@Composable
fun PdfPageView(
    document: PdfDocument,
    pageIndex: Int,
    nightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var pageBitmap by remember(pageIndex, nightMode) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pageIndex, nightMode) { mutableStateOf(true) }

    var scale by remember(pageIndex) { mutableFloatStateOf(1f) }
    var offset by remember(pageIndex) { mutableStateOf(Offset.Zero) }

    // Render the page in background coroutine
    LaunchedEffect(pageIndex, nightMode) {
        isLoading = true
        val bmp = document.renderPage(pageIndex, scale = 2.0f, nightMode = nightMode)
        pageBitmap = bmp
        isLoading = false
    }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        if (newScale > 1f) {
            val maxOffset = 1000f * (newScale - 1f)
            offset = Offset(
                x = (offset.x + offsetChange.x * newScale).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + offsetChange.y * newScale).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (nightMode) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(pageIndex) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1.5f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = state),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (pageBitmap != null) {
            Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }
    }
}
