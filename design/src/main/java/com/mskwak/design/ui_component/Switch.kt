package com.mskwak.design.ui_component

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.design.util.clickableWithoutRipple

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    width: Dp = 36.dp,
    padding: Dp = 2.dp
) {
    val height = width * 0.55f
    val thumbSize = height - padding * 2
    val thumbPosition by animateDpAsState(
        targetValue = if (checked) width - thumbSize - padding else padding
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                color = if (checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                shape = CircleShape
            )
            .clickableWithoutRipple {
                onCheckedChange(!checked)
            }
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .align(Alignment.CenterStart)
                .offset(x = thumbPosition)
                .background(Color.White, CircleShape)

        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    var checked by remember { mutableStateOf(true) }

    GardenLogTheme {
        Switch(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}