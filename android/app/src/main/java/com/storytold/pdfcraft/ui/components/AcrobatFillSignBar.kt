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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.TextFields
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

enum class FillSignTool {
    NONE,
    TEXT,
    CHECKMARK,
    CROSS,
    DATE,
    SIGNATURE
}

@Composable
fun AcrobatFillSignBar(
    activeTool: FillSignTool,
    onSelectTool: (FillSignTool) -> Unit,
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
                    text = "Fill & Sign",
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
                FillSignIconItem(
                    label = "Text",
                    icon = Icons.Default.TextFields,
                    isSelected = activeTool == FillSignTool.TEXT,
                    onClick = { onSelectTool(FillSignTool.TEXT) }
                )
                FillSignIconItem(
                    label = "Check",
                    icon = Icons.Default.Check,
                    isSelected = activeTool == FillSignTool.CHECKMARK,
                    onClick = { onSelectTool(FillSignTool.CHECKMARK) }
                )
                FillSignIconItem(
                    label = "Cross",
                    icon = Icons.Default.Close,
                    isSelected = activeTool == FillSignTool.CROSS,
                    onClick = { onSelectTool(FillSignTool.CROSS) }
                )
                FillSignIconItem(
                    label = "Date",
                    icon = Icons.Default.CalendarToday,
                    isSelected = activeTool == FillSignTool.DATE,
                    onClick = { onSelectTool(FillSignTool.DATE) }
                )
                FillSignIconItem(
                    label = "Signature",
                    icon = Icons.Default.Draw,
                    isSelected = activeTool == FillSignTool.SIGNATURE,
                    onClick = { onSelectTool(FillSignTool.SIGNATURE) }
                )
            }
        }
    }
}

@Composable
private fun FillSignIconItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) AcrobatActionBlue else Color.Transparent),
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
            color = if (isSelected) AcrobatActionBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
