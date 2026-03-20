package com.mskwak.plant.plant_list

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.WaterDropBlue
import com.mskwak.common_ui.icon.WaterDropRed
import com.mskwak.common_ui.icon.WaterDropWithBackground
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.util.clickableWithoutRipple
import com.mskwak.common_ui.util.toDateString
import com.mskwak.plant.R
import com.mskwak.plant.model.PlantListItemUiModel
import com.mskwak.plant.model.WateringStatus
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PlantListItem(
    modifier: Modifier = Modifier,
    uiModel: PlantListItemUiModel,
    onEvent: (PlantListEvent) -> Unit
) {
    val revealState = rememberSwipeToRevealState(80.dp)
    val scope = rememberCoroutineScope()

    if (uiModel.isHarvested) {
        // 수확된 식물은 스와이프(물주기) 비활성화
        ForegroundCard(
            modifier = modifier,
            uiModel = uiModel,
            onClick = { onEvent(PlantListEvent.OnPlantClicked(uiModel.plantId)) }
        )
    } else {
        SwipeToRevealCard(
            modifier = modifier.height(IntrinsicSize.Min),
            state = revealState,
            backgroundContent = {
                BackgroundCard(
                    onClick = {
                        scope.launch { revealState.animateTo(RevealState.CLOSED) }
                        onEvent(PlantListEvent.Watering(uiModel.plantId))
                    }
                )
            },
            cardContent = {
                ForegroundCard(
                    uiModel = uiModel,
                    onClick = {
                        scope.launch { revealState.animateTo(RevealState.CLOSED) }
                        onEvent(PlantListEvent.OnPlantClicked(uiModel.plantId))
                    }
                )
            }
        )
    }
}

@Composable
private fun BackgroundCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(10.dp)
            )
            .clickableWithoutRipple(onClick = onClick),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = stringResource(R.string.watering),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun ForegroundCard(
    modifier: Modifier = Modifier,
    uiModel: PlantListItemUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(10.dp)
            )
            .clickableWithoutRipple(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageModifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(10.dp))

        if (uiModel.imagePath == null) {
            Image(
                painterResource(R.drawable.ic_flower_pot),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = imageModifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(8.dp)
            )
        } else {
            AsyncImage(
                model = uiModel.imagePath,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_flower_pot),
                error = painterResource(R.drawable.ic_flower_pot),
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = uiModel.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.plant_date_format, uiModel.createdAt.toDateString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiModel.harvestDate != null) {
                Text(
                    text = stringResource(
                        R.string.harvest_date_format,
                        uiModel.harvestDate.toDateString()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!uiModel.isHarvested) {
            Image(
                imageVector = when (uiModel.status) {
                    WateringStatus.OVERDUE -> IconPack.WaterDropRed
                    WateringStatus.TODAY_DONE -> IconPack.WaterDropWithBackground
                    else -> IconPack.WaterDropBlue
                },
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )

            Spacer(Modifier.width(6.dp))
            Text(
                text = when (uiModel.status) {
                    WateringStatus.TODAY, WateringStatus.TODAY_DONE -> {
                        stringResource(R.string.today)
                    }

                    WateringStatus.OVERDUE, WateringStatus.NO_PERIOD -> {
                        stringResource(R.string.watering_d_day_plus_format, uiModel.dDay)
                    }

                    WateringStatus.UPCOMING -> {
                        stringResource(R.string.watering_d_day_minus_format, uiModel.dDay)
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private class WateringStatusPreviewProvider : PreviewParameterProvider<WateringStatus> {
    override val values: Sequence<WateringStatus> = sequenceOf(
        WateringStatus.OVERDUE,
        WateringStatus.TODAY,
        WateringStatus.TODAY_DONE,
        WateringStatus.UPCOMING,
        WateringStatus.NO_PERIOD
    )
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlantListItemPreview(
    @PreviewParameter(WateringStatusPreviewProvider::class) status: WateringStatus
) {
    val sampleData = PlantListItemUiModel(
        plantId = 1,
        name = "몬스테라",
        imagePath = null,
        dDay = 1,
        status = status,
        createdAt = LocalDate.now()
    )
    GardenLogTheme {
        PlantListItem(
            uiModel = sampleData,
            onEvent = {}
        )
    }
}