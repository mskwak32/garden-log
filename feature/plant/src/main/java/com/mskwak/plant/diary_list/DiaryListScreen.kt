package com.mskwak.plant.diary_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import com.mskwak.design.IconPack
import com.mskwak.design.icon.ArrowBackIosBlack
import com.mskwak.design.icon.ArrowForwardIosBlack
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.plant.R
import com.mskwak.plant.model.DiaryListItemUiModel
import java.time.LocalDate
import java.time.YearMonth

@Serializable
data object DiaryListScreen : NavKey

@Composable
fun DiaryListScreen(
    viewModel: DiaryListViewModel = hiltViewModel(),
    navigate: (DiaryListEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    var selectYearMonthDialogState: YearMonth? by remember { mutableStateOf(null) }

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    selectYearMonthDialogState?.let { yearMonth ->
        SelectYearMonthDialog(
            initialYearMonth = yearMonth,
            onDismiss = { selectYearMonthDialogState = null },
            onConfirm = { selectedYearMonth ->
                viewModel.setEvent(DiaryListEvent.YearMonthChanged(selectedYearMonth))
                selectYearMonthDialogState = null
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryListEffect.ShowSelectYearMonth -> {
                    selectYearMonthDialogState = effect.yearMonth
                }

                is DiaryListEffect.Navigation -> {
                    navigate(effect)
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: DiaryListState,
    onEvent: (DiaryListEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(onEvent, state.currentYearMonth)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 8.dp)
                .fillMaxHeight()
        ) {
            DiaryListPlantFilter(
                currentFilter = state.currentPlant,
                plantList = state.plantFilterList,
                onEvent = onEvent
            )

            Spacer(Modifier.height(8.dp))
            DiaryListSortFilter(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentSortOrder = state.sortOder,
                onEvent = onEvent
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            if (state.diaries.isEmpty()) {
                ListEmptyView(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.diaries,
                        key = { it.id }
                    ) { item ->
                        DiaryListItem(
                            diary = item,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onEvent: (DiaryListEvent) -> Unit,
    yearMonth: YearMonth
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = IconPack.ArrowBackIosBlack,
                    contentDescription = stringResource(R.string.previous_month),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = { onEvent(DiaryListEvent.OnPreviousMonthClicked) })
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = yearMonth.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable(
                        onClick = { onEvent(DiaryListEvent.SelectMonth) }
                    )
                )
                Spacer(Modifier.width(16.dp))
                Image(
                    imageVector = IconPack.ArrowForwardIosBlack,
                    contentDescription = stringResource(R.string.next_month),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = { onEvent(DiaryListEvent.OnNextMonthClicked) })
                )
            }
        },
        actions = {
            Text(
                text = stringResource(R.string.this_month),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        onClick = { onEvent(DiaryListEvent.OnThisMonthClicked) }
                    )
                    .padding(10.dp)
            )
        }
    )
}

@Composable
private fun ListEmptyView(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isPreview) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
            )
        } else {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_notebook))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.diary_list_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Content - List")
@Composable
private fun ContentPreview() {
    val now = LocalDate.now()
    GardenLogTheme {
        val samplePlantFilter = DiaryListPlantFilterUiModel(
            plantId = 1,
            plantName = "Rose"
        )
        val sampleDiary = DiaryListItemUiModel(
            id = 1,
            date = now,
            content = "Watered the rose plant and it seems to be doing well. The leaves are very green and healthy.",
            imagePath = null,
            plantName = "Rose"
        )
        val sampleState = DiaryListState(
            currentYearMonth = YearMonth.of(2023, 10),
            currentPlant = samplePlantFilter,
            plantFilterList = listOf(
                samplePlantFilter,
                samplePlantFilter.copy(plantId = 2, plantName = "Tulip")
            ),
            sortOder = DiaryListSortOrder.CREATED_LATEST,
            diaries = listOf(
                sampleDiary,
                sampleDiary.copy(
                    id = 2,
                    date = now.minusDays(2),
                    plantName = "Tulip",
                    content = "Tulip is blooming!"
                ),
                sampleDiary.copy(
                    id = 3,
                    date = now.minusDays(3),
                    plantName = "Rose",
                    content = "Found a pest on the rose, applied treatment."
                )
            )
        )

        Content(
            state = sampleState,
            onEvent = {}
        )
    }
}

@Preview(name = "Content - Empty")
@Composable
private fun ContentEmptyPreview() {
    GardenLogTheme {
        val sampleState = DiaryListState(
            currentYearMonth = YearMonth.of(2023, 10),
            diaries = emptyList()
        )

        Content(
            state = sampleState,
            onEvent = {}
        )
    }
}