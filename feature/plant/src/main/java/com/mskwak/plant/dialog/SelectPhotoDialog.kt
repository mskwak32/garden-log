package com.mskwak.plant.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.AddPhotoBlack
import com.mskwak.common_ui.icon.CloseBlack
import com.mskwak.common_ui.icon.ImageBlack
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.util.clickableWithoutRipple
import com.mskwak.plant.R

enum class PhotoAction {
    GALLERY,
    CAMERA,
    DELETE
}

@Composable
fun SelectPhotoDialog(
    showDeleteButton: Boolean = false,
    onDismiss: () -> Unit,
    onAction: (PhotoAction) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhotoActionItem(
                icon = { Icon(IconPack.ImageBlack, null, Modifier.size(30.dp)) },
                label = stringResource(R.string.photo_image),
                onClick = { onAction(PhotoAction.GALLERY) }
            )
            PhotoActionItem(
                icon = { Icon(IconPack.AddPhotoBlack, null, Modifier.size(30.dp)) },
                label = stringResource(R.string.photo_camera),
                onClick = { onAction(PhotoAction.CAMERA) }
            )
            if (showDeleteButton) {
                PhotoActionItem(
                    icon = { Icon(IconPack.CloseBlack, null, Modifier.size(30.dp)) },
                    label = stringResource(R.string.delete),
                    onClick = { onAction(PhotoAction.DELETE) }
                )
            }
        }
    }
}

@Composable
private fun PhotoActionItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clickableWithoutRipple(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun PreviewWithDelete() {
    GardenLogTheme {
        SelectPhotoDialog(
            showDeleteButton = true,
            onDismiss = {},
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun PreviewWithoutDelete() {
    GardenLogTheme {
        SelectPhotoDialog(
            showDeleteButton = false,
            onDismiss = {},
            onAction = {}
        )
    }
}