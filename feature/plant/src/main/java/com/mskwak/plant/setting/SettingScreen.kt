package com.mskwak.plant.setting

import android.widget.Toast
import com.mskwak.common_ui.util.findActivity
import com.mskwak.common_ui.util.openPlayStore
import com.google.android.play.core.review.ReviewManagerFactory
import com.mskwak.plant.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.common_ui.dialog.NotReadyDialog
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.LocalNavBottomBarPadding
import com.mskwak.plant.R

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var showNotReadyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val debugToastMessage = stringResource(R.string.setting_play_store_debug)

    if (showNotReadyDialog) {
        NotReadyDialog(onDismiss = { showNotReadyDialog = false })
    }

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingEffect.ShowNotReadyDialog -> showNotReadyDialog = true
                SettingEffect.OpenPlayStore -> {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(
                            context,
                            debugToastMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val activity = context.findActivity()
                        if (activity != null) {
                            val reviewManager = ReviewManagerFactory.create(context)
                            reviewManager.requestReviewFlow().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    reviewManager.launchReviewFlow(activity, task.result)
                                } else {
                                    context.openPlayStore()
                                }
                            }
                        } else {
                            context.openPlayStore()
                        }
                    }
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
