package com.mskwak.common_ui.ui_component

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowForwardIosBlack
import com.mskwak.common_ui.icon.WaterDropBlue
import com.mskwak.common_ui.theme.GardenLogTheme

@Composable
fun LabeledClickableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(6.dp))
                trailingIcon()
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = IconPack.ArrowForwardIosBlack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Light - 아이콘 없음", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark - 아이콘 없음", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWithoutTrailingIcon() {
    GardenLogTheme {
        LabeledClickableField(
            label = "날짜",
            value = "2026년 3월 22일",
            onClick = {}
        )
    }
}

@Preview(name = "Light - 아이콘 있음", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark - 아이콘 있음", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWithTrailingIcon() {
    GardenLogTheme {
        LabeledClickableField(
            label = "날짜",
            value = "2026년 3월 22일",
            onClick = {},
            trailingIcon = {
                Icon(
                    imageVector = IconPack.WaterDropBlue,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
        )
    }
}