package com.mskwak.plant.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskwak.common_ui.Screen

data object SettingScreen : Screen

@Composable
fun SettingScreen(

) {
    Content()
}

@Composable
private fun Content() {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}