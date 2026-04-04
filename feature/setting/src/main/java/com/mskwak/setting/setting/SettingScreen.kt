package com.mskwak.setting.setting

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.LocalNavBottomBarPadding
import com.mskwak.common_ui.util.openPlayStore
import com.mskwak.domain.Constants
import com.mskwak.setting.BuildConfig
import com.mskwak.setting.R

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToExportedDiaryList: () -> Unit = {}
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val debugToastMessage = stringResource(R.string.setting_play_store_debug)

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingEffect.OpenUpdateLog -> {
                    val intent = Intent(Intent.ACTION_VIEW, Constants.APP_UPDATE_LOG_URL.toUri())
                    context.startActivity(intent)
                }

                SettingEffect.OpenPlayStore -> {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(
                            context,
                            debugToastMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        context.openPlayStore()
                    }
                }

                SettingEffect.NavigateToExportedDiaryList -> {
                    onNavigateToExportedDiaryList()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: SettingState,
    onEvent: (SettingEvent) -> Unit
) {
    Scaffold(
        topBar = { TopBar() },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            .union(WindowInsets(bottom = LocalNavBottomBarPadding.current))
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SettingItem(
                label = stringResource(R.string.setting_version),
                value = state.versionName
            )
            SettingItem(
                label = stringResource(R.string.setting_update_content),
                onClick = { onEvent(SettingEvent.UpdateContentClick) }
            )
            SettingItem(
                label = stringResource(R.string.setting_app_estimation),
                onClick = { onEvent(SettingEvent.RateAppClick) }
            )
            SettingItem(
                label = stringResource(R.string.setting_exported_diary),
                onClick = { onEvent(SettingEvent.ExportedDiaryListClick) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        modifier = Modifier.padding(horizontal = 8.dp),
        title = {
            Text(
                text = stringResource(R.string.setting_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    )
}

@Composable
private fun SettingItem(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    GardenLogTheme {
        Content(
            state = SettingState(versionName = "1.0.0"),
            onEvent = {}
        )
    }
}
