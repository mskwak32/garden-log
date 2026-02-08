package com.mskwak.design.theme

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