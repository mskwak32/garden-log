package com.mskwak.common_ui.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mskwak.common_ui.R
import com.mskwak.common_ui.theme.GardenLogTheme

@Composable
fun NotReadyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.lottie_under_construction)
                )
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(140.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(text = stringResource(R.string.message_not_ready))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}

@Preview
@Composable
private fun NotReadyDialogPreview() {
    GardenLogTheme {
        NotReadyDialog(onDismiss = {})
    }
}
