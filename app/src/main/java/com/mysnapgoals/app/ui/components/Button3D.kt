package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.theme.Milk
import com.mysnapgoals.app.ui.theme.Mocha
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun Button3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    depth: Dp = 6.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(height + depth)
            .wrapContentWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Base (grosor)
        Box(
            modifier = Modifier
                .offset(y = depth)
                .height(height)
                .fillMaxWidth()
                .background(
                    color = Mocha,
                    shape = RoundedCornerShape(34.dp)
                )
        )

        // Cara superior
        Box(
            modifier = Modifier
                .offset(y = if (pressed) depth else 0.dp)
                .height(height)
                .fillMaxWidth()
                .shadow(
                    elevation = if (pressed) 2.dp else 8.dp,
                    shape = RoundedCornerShape(34.dp),
                    clip = false
                )
                .background(
                    color = Milk,
                    shape = RoundedCornerShape(34.dp),
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewButton3D(){
    SnapGoalsTheme {
        Button3D(
            text = "Confirm",
            onClick = {}
        )
    }
}