package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Panel3D(
    modifier: Modifier = Modifier,
    depth: Dp = 6.dp,
    elevation: Dp = 10.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    topColor: Color = MaterialTheme.colorScheme.surface,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.padding(bottom = depth)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = depth)
                .background(color = baseColor, shape = shape)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(elevation = elevation, shape = shape, clip = false)
                .background(color = topColor, shape = shape)
        )

        Box {
            content()
        }
    }
}
