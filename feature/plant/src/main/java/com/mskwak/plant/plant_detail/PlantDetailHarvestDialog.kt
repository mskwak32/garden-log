package com.mskwak.plant.plant_detail

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.plant.R

@Composable
fun PlantDetailHarvestDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.harvest)) },
        text = { Text(stringResource(R.string.message_harvest_confirm)) }
    )
}

@Composable
@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun Preview() {
    GardenLogTheme {
        PlantDetailHarvestDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
