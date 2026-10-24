package com.storytold.pdfcraft.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytold.pdfcraft.core.PdfDocument
import com.storytold.pdfcraft.ui.theme.AcrobatActionBlue
import com.storytold.pdfcraft.ui.theme.AcrobatRed

data class PageStamp(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: FillSignTool,
    var x: Float,
    var y: Float,
    var text: String = "",
    val bitmap: Bitmap? = null
)

data class PageCommentNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var text: String = ""
)

/**
 * Interactive touch canvas for a PDF page with Acrobat gestures,
 * pinch-to-zoom, Fill & Sign draggable stamp placement, freehand drawing, and sticky notes.
 */
@Composable
fun PdfPageView(
    document: PdfDocument,
    pageIndex: Int,
    nightMode: Boolean = false,
    activeFillSignTool: FillSignTool = FillSignTool.NONE,
    activeCommentTool: CommentTool = CommentTool.NONE,
    onBackgroundTap: () -> Unit = {},
    onFillSignTap: (Offset) -> Unit = {},
    onCommentTap: (Offset) -> Unit = {},
    stamps: List<PageStamp> = emptyList(),
    notes: List<PageCommentNote> = emptyList(),
    onDeleteStamp: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var pageBitmap by remember(pageIndex, nightMode) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pageIndex, nightMode) { mutableStateOf(true) }

    var scale by remember(pageIndex) { mutableFloatStateOf(1f) }
    var offset by remember(pageIndex) { mutableStateOf(Offset.Zero) }
    var selectedStampId by remember { mutableStateOf<String?>(null) }

    // Freehand drawing strokes for markup
    val drawingStrokes = remember { mutableStateListOf<StrokePath>() }
    var currentDrawPoints = remember { mutableStateListOf<Offset>() }

    // Render the page in background coroutine
    LaunchedEffect(pageIndex, nightMode) {
        isLoading = true
        val bmp = document.renderPage(pageIndex, scale = 2.0f, nightMode = nightMode)
        pageBitmap = bmp
        isLoading = false
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (activeFillSignTool == FillSignTool.NONE && activeCommentTool != CommentTool.PEN) {
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
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (nightMode) Color(0xFF1E1E1E) else Color(0xFFEBEBEB))
            .pointerInput(pageIndex, activeFillSignTool, activeCommentTool) {
                detectTapGestures(
                    onDoubleTap = {
                        if (activeFillSignTool == FillSignTool.NONE && activeCommentTool == CommentTool.NONE) {
                            scale = if (scale > 1.5f) 1f else 2.5f
                            offset = Offset.Zero
                        }
                    },
                    onTap = { tapOffset ->
                        if (activeFillSignTool != FillSignTool.NONE) {
                            onFillSignTap(tapOffset)
                        } else if (activeCommentTool == CommentTool.STICKY_NOTE) {
                            onCommentTap(tapOffset)
                        } else {
                            selectedStampId = null
                            onBackgroundTap()
                        }
                    }
                )
            }
            .then(
                if (activeCommentTool == CommentTool.PEN) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                currentDrawPoints.clear()
                                currentDrawPoints.add(startOffset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentDrawPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentDrawPoints.isNotEmpty()) {
                                    drawingStrokes.add(
                                        StrokePath(
                                            points = currentDrawPoints.toList(),
                                            color = Color(0xFFD32F2F),
                                            strokeWidth = 5f
                                        )
                                    )
                                    currentDrawPoints.clear()
                                }
                            }
                        )
                    }
                } else {
                    Modifier.transformable(state = transformState)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = AcrobatRed)
        } else if (pageBitmap != null) {
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .shadow(8.dp, RoundedCornerShape(2.dp))
                    .background(Color.White)
            ) {
                // PDF Rendered Image
                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "PDF Page ${pageIndex + 1}"
                )

                // Freehand Drawing Layer
                androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                    for (stroke in drawingStrokes) {
                        if (stroke.points.size > 1) {
                            val p = androidx.compose.ui.graphics.Path()
                            p.moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                p.lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                            drawPath(
                                path = p,
                                color = stroke.color,
                                style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                    if (currentDrawPoints.size > 1) {
                        val activeP = androidx.compose.ui.graphics.Path()
                        activeP.moveTo(currentDrawPoints[0].x, currentDrawPoints[0].y)
                        for (i in 1 until currentDrawPoints.size) {
                            activeP.lineTo(currentDrawPoints[i].x, currentDrawPoints[i].y)
                        }
                        drawPath(
                            path = activeP,
                            color = Color(0xFFD32F2F),
                            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                // Render Stamped Items (Checkmarks, Crosses, Dates, Signatures) with drag & delete handle
                for (stamp in stamps) {
                    var stampX by remember(stamp.id) { mutableFloatStateOf(stamp.x) }
                    var stampY by remember(stamp.id) { mutableFloatStateOf(stamp.y) }
                    val isStampSelected = selectedStampId == stamp.id

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(stampX.toInt(), stampY.toInt()) }
                            .pointerInput(stamp.id) {
                                detectDragGestures(
                                    onDragStart = { selectedStampId = stamp.id },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        stampX += dragAmount.x
                                        stampY += dragAmount.y
                                        stamp.x = stampX
                                        stamp.y = stampY
                                    }
                                )
                            }
                            .clickable { selectedStampId = stamp.id }
                            .border(
                                width = if (isStampSelected) 1.5.dp else 0.dp,
                                color = if (isStampSelected) AcrobatActionBlue else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    ) {
                        when (stamp.type) {
                            FillSignTool.CHECKMARK -> {
                                Text(
                                    text = "✓",
                                    fontSize = 28.sp,
                                    color = AcrobatActionBlue
                                )
                            }
                            FillSignTool.CROSS -> {
                                Text(
                                    text = "✗",
                                    fontSize = 28.sp,
                                    color = AcrobatRed
                                )
                            }
                            FillSignTool.DATE -> {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xEEFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AcrobatActionBlue)
                                ) {
                                    Text(
                                        text = stamp.text,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            FillSignTool.TEXT -> {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xDDFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AcrobatActionBlue)
                                ) {
                                    Text(
                                        text = stamp.text,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            FillSignTool.SIGNATURE -> {
                                if (stamp.bitmap != null) {
                                    Image(
                                        bitmap = stamp.bitmap.asImageBitmap(),
                                        contentDescription = "Signature Stamp",
                                        modifier = Modifier
                                            .size(120.dp, 60.dp)
                                    )
                                }
                            }
                            else -> {}
                        }

                        // Acrobat Delete Handle (Shown when selected)
                        if (isStampSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(AcrobatRed)
                                    .clickable { onDeleteStamp(stamp.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Stamp",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Render Sticky Notes
                for (note in notes) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(note.x.toInt(), note.y.toInt()) }
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD54F))
                            .shadow(4.dp, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ModeComment,
                            contentDescription = "Note",
                            tint = Color(0xFF424242),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
