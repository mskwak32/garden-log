package com.mskwak.gardendailylog.ui.dialog

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.gardendailylog.R

@Composable
fun ForceUpdateDialog() {
    val context = LocalContext.current
    val activity = context as? Activity
    AlertDialog(
        onDismissRequest = { activity?.finish() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = stringResource(R.string.force_update_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.force_update_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val packageName = "com.mskwak.gardendailylog"
                    val intent = try {
                        Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
                    } catch (_: ActivityNotFoundException) {
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$packageName".toUri()
                        )
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.force_update_button))
            }
        }
    )
}

@Preview
@Composable
private fun ForceUpdateDialogPreview() {
    GardenLogTheme {
        ForceUpdateDialog()
    }
}
