package com.mskwak.common_ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.mskwak.design.theme.GardenLogTheme

@Composable
fun PreviewTheme(
    content: @Composable () -> Unit
) {
    GardenLogTheme {
        Surface {
            content()
        }
    }
}