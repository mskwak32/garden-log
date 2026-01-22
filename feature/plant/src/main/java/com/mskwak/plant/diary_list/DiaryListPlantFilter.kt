package com.mskwak.plant.diary_list

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.plant.R
import kotlinx.coroutines.launch

/**
 * @param currentFilter null일 경우 전체선택
 */
@Composable
fun DiaryListPlantFilter(
    modifier: Modifier = Modifier,
    currentFilter: DiaryListPlantFilterUiModel?,
    plantList: List<DiaryListPlantFilterUiModel>,
    onEvent: (DiaryListEvent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val filters = remember(plantList) { listOf(null) + plantList }

    LazyRow(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(filters, key = { _, item -> item?.plantId ?: -1 }) { index, filterItem ->
            val isSelected = currentFilter == filterItem

            FilterChip(
                text = filterItem?.plantName ?: stringResource(R.string.select_all),
                isSelected = isSelected,
                onClick = {
                    if (currentFilter != filterItem) {
                        onEvent(DiaryListEvent.PlantFilterChanged(filterItem))
                    }
                    scope.launch {
                        // First, find the item in the layout info
                        val itemInfo =
                            lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                        if (itemInfo != null) {
                            // If visible, calculate scroll amount to center it
                            val containerCenter = lazyListState.layoutInfo.viewportSize.width / 2
                            val itemCenter = itemInfo.offset + itemInfo.size / 2
                            val scrollDelta = itemCenter - containerCenter
                            lazyListState.animateScrollBy(scrollDelta.toFloat())
                        } else {
                            // If not visible, just scroll to the item.
                            lazyListState.animateScrollToItem(index)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val borderModifier = if (isSelected) {
        Modifier
    } else {
        Modifier.border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(50)
        )
    }

    Box(
        modifier = Modifier
            .then(borderModifier)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .widthIn(min = 60.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlantFilterPreview() {
    val samplePlantList = listOf(
        DiaryListPlantFilterUiModel(1, "Rose"),
        DiaryListPlantFilterUiModel(2, "Lily"),
        DiaryListPlantFilterUiModel(3, "Tulip")
    )
    var currentFilter: DiaryListPlantFilterUiModel? by remember { mutableStateOf(samplePlantList[0]) }

    GardenLogTheme {
        Surface {
            DiaryListPlantFilter(
                modifier = Modifier.padding(vertical = 10.dp),
                currentFilter = currentFilter,
                plantList = samplePlantList,
                onEvent = { event ->
                    if (event is DiaryListEvent.PlantFilterChanged) {
                        currentFilter = event.plantFilter
                    }
                }
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlantFilterPreview_NotSelected() {
    val samplePlantList = listOf(
        DiaryListPlantFilterUiModel(1, "Rose"),
        DiaryListPlantFilterUiModel(2, "Lily"),
        DiaryListPlantFilterUiModel(3, "Tulip")
    )
    var currentFilter: DiaryListPlantFilterUiModel? by remember { mutableStateOf(null) }

    GardenLogTheme {
        Surface {
            DiaryListPlantFilter(
                modifier = Modifier.padding(vertical = 10.dp),
                currentFilter = currentFilter,
                plantList = samplePlantList,
                onEvent = { event ->
                    if (event is DiaryListEvent.PlantFilterChanged) {
                        currentFilter = event.plantFilter
                    }
                }
            )
        }
    }
}