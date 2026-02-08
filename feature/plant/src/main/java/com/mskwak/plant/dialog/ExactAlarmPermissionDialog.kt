package com.mskwak.plant.dialog

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mskwak.plant.R

@Composable
fun ExactAlarmPermissionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exact_alarm_permission_title)) },
        text = { Text(stringResource(R.string.exact_alarm_permission_message)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                openExactAlarmSettings(context)
            }) {
                Text(stringResource(R.string.permission_setting_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        )
    }
}