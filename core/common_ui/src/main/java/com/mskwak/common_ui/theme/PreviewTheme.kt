package com.mskwak.common_ui.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

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