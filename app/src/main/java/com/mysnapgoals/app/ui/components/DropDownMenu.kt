package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DropDownMenu(
    modifier: Modifier = Modifier,
    label: String = "Price.",
    height: Dp = 46.dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color(0xFFF6EFE6),
        shadowElevation = 18.dp, // CLAVE para “botado”
        tonalElevation = 0.dp,
        onClick = onClick
    ) {
        Box(
            modifier =
                Modifier
                    .height(height)
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = shape
                    )
        ) {
            // Highlight superior (efecto glass / lift)
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                )
                        )
            )

            Text(
                text = label,
                color = Color(0xFF1F1F1F),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp)
            )

            // Right floating circle
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(height - 8.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFF1F1F1F)
                        ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFFF6EFE6),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun previewDropDownMenu(){
    DropDownMenu() { }
}