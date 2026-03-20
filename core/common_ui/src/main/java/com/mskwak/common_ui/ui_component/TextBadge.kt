package com.mskwak.common_ui.ui_component

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.theme.PreviewTheme

@Composable
fun TextBadge(
    modifier: Modifier = Modifier,
    text: String,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = borderColor,
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    PreviewTheme {
        TextBadge(text = "수확")
    }
}
