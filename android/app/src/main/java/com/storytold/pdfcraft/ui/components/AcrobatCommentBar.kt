package com.storytold.pdfcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytold.pdfcraft.ui.theme.AcrobatActionBlue

enum class CommentTool {
    NONE,
    HIGHLIGHT,
    UNDERLINE,
    STRIKETHROUGH,
    PEN,
    STICKY_NOTE
}

@Composable
fun AcrobatCommentBar(
    activeTool: CommentTool,
    onSelectTool: (CommentTool) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comment & Markup",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = AcrobatActionBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = "Done", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommentIconItem(
                    label = "Highlight",
                    icon = Icons.Default.BorderColor,
                    isSelected = activeTool == CommentTool.HIGHLIGHT,
                    activeColor = Color(0xFFFBC02D),
                    onClick = { onSelectTool(CommentTool.HIGHLIGHT) }
                )
                CommentIconItem(
                    label = "Underline",
                    icon = Icons.Default.FormatUnderlined,
                    isSelected = activeTool == CommentTool.UNDERLINE,
                    activeColor = AcrobatActionBlue,
                    onClick = { onSelectTool(CommentTool.UNDERLINE) }
                )
                CommentIconItem(
                    label = "Strike",
                    icon = Icons.Default.FormatStrikethrough,
                    isSelected = activeTool == CommentTool.STRIKETHROUGH,
                    activeColor = Color(0xFFE53935),
                    onClick = { onSelectTool(CommentTool.STRIKETHROUGH) }
                )
                CommentIconItem(
                    label = "Draw",
                    icon = Icons.Default.Create,
                    isSelected = activeTool == CommentTool.PEN,
                    activeColor = Color(0xFF7E57C2),
                    onClick = { onSelectTool(CommentTool.PEN) }
                )
                CommentIconItem(
                    label = "Note",
                    icon = Icons.Default.ModeComment,
                    isSelected = activeTool == CommentTool.STICKY_NOTE,
                    activeColor = Color(0xFF00ACC1),
                    onClick = { onSelectTool(CommentTool.STICKY_NOTE) }
                )
            }
        }
    }
}

@Composable
private fun CommentIconItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) activeColor else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
