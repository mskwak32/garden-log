package com.mskwak.plant.diary_list

import android.content.res.Configuration
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.util.toDateWithDayOfWeek
import com.mskwak.plant.R
import com.mskwak.plant.model.DiaryListItemUiModel
import java.time.LocalDate

@Composable
fun DiaryListItem(
    diary: DiaryListItemUiModel,
    onEvent: (DiaryListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onEvent(DiaryListEvent.OnDiaryClicked(diary.id)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val imageModifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))

            if (diary.imagePath == null) {
                Image(
                    painter = painterResource(R.drawable.ic_flower_pot),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(12.dp)
                )
            } else {
                AsyncImage(
                    model = diary.imagePath,
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.ic_flower_pot),
                    error = painterResource(R.drawable.ic_flower_pot),
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }

            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = diary.date.toDateWithDayOfWeek(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(6.dp))
                if (diary.plantName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_leaf),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = diary.plantName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Text(
                    text = diary.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiaryListItemPreview() {
    val now = LocalDate.now()

    GardenLogTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            DiaryListItem(
                diary = DiaryListItemUiModel(
                    id = 1,
                    date = now,
                    content = "오늘 몬스테라에게 물을 주었다. 새 잎이 나오려고 하는 것 같아서 기분이 좋다. 햇빛을 더 많이 받게 해줘야겠다.",
                    imagePath = null,
                    plantName = "몬스테라"
                ),
                onEvent = {}
            )
            Spacer(Modifier.height(12.dp))
            DiaryListItem(
                diary = DiaryListItemUiModel(
                    id = 2,
                    date = now.minusDays(3),
                    content = "잎이 조금 노래졌다.",
                    imagePath = "/path/to/image.jpg",
                    plantName = "스투키"
                ),
                onEvent = {}
            )
        }
    }
}