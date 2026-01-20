package com.mskwak.plant.plant_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.plant.R

@Composable
fun PlantDetailDiaryItem(
    modifier: Modifier = Modifier,
    diary: DiaryUiModel,
    onClick: (DiaryUiModel) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row {
            if (diary.imagePath == null) {
                Image(
                    painter = painterResource(R.drawable.ic_flower_pot),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceBright,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(5.dp)
                )
            } else {
                AsyncImage(
                    model = diary.imagePath,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = diary.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = diary.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    minLines = 2
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun PlantDetailDiaryItemPreview() {
    GardenLogTheme {
        PlantDetailDiaryItem(
            diary = DiaryUiModel(
                id = 0,
                date = "2024/01/01",
                content = "content",
                imagePath = null
            ),
            onClick = {}
        )
    }
}