package com.storytold.pdfcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
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

@Composable
fun AcrobatSpeedDial(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onFillAndSign: () -> Unit,
    onComment: () -> Unit,
    onOrganize: () -> Unit,
    onExtractText: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedDialItem(
                    label = "Comment & Markup",
                    icon = Icons.Default.ModeComment,
                    color = Color(0xFFFBC02D),
                    iconTint = Color.Black,
                    onClick = {
                        onToggleExpand()
                        onComment()
                    }
                )
                SpeedDialItem(
                    label = "Fill & Sign",
                    icon = Icons.Default.Draw,
                    color = AcrobatActionBlue,
                    iconTint = Color.White,
                    onClick = {
                        onToggleExpand()
                        onFillAndSign()
                    }
                )
                SpeedDialItem(
                    label = "Organize Pages",
                    icon = Icons.Default.FormatListNumbered,
                    color = Color(0xFF7E57C2),
                    iconTint = Color.White,
                    onClick = {
                        onToggleExpand()
                        onOrganize()
                    }
                )
                SpeedDialItem(
                    label = "Extract Text",
                    icon = Icons.Default.TextFields,
                    color = Color(0xFF26A69A),
                    iconTint = Color.White,
                    onClick = {
                        onToggleExpand()
                        onExtractText()
                    }
                )
            }
        }

        // Main Acrobat Floating Action Button (Iconic Blue Circle)
        FloatingActionButton(
            onClick = onToggleExpand,
            shape = CircleShape,
            containerColor = AcrobatActionBlue,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Edit,
                contentDescription = if (isExpanded) "Close tools" else "Open Acrobat tools",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    color: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xEE2A2A2A),
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = color,
            contentColor = iconTint,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
