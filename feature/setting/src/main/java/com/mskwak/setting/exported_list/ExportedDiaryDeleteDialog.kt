package com.mskwak.setting.exported_list

import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.setting.R

@Composable
fun ExportedDiaryDeleteDialog(
    uri: Uri,
    onConfirm: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = stringResource(R.string.exported_diary_delete_confirm),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(uri) }) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
private fun ExportedDiaryDeleteDialogPreview() {
    GardenLogTheme {
        ExportedDiaryDeleteDialog(
            uri = Uri.EMPTY,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
