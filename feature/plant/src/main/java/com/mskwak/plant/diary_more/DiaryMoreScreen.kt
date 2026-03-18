package com.mskwak.plant.diary_more

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowBackBlack
import com.mskwak.common_ui.icon.CaretDown
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.AppDropDownMenu
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.plant.R
import com.mskwak.plant.diary_list.DiaryListEvent
import com.mskwak.plant.diary_list.DiaryListItem
import com.mskwak.plant.model.DiaryListItemUiModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DiaryMoreScreen(
    viewModel: DiaryMoreViewModel = hiltViewModel(),
    navigate: (DiaryMoreEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiaryMoreEffect.Navigation -> navigate(effect)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    plantName: String,
    sortOrder: DiaryListSortOrder,
    onEvent: (DiaryMoreEvent) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = plantName,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(DiaryMoreEvent.BackClick) }) {
                Image(
                    imageVector = IconPack.ArrowBackBlack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            SortButton(
                currentSortOrder = sortOrder,
                onSortOrderChanged = { onEvent(DiaryMoreEvent.SortOrderChanged(it)) }
            )
        }
    )
}

@Composable
private fun Content(
    state: DiaryMoreState,
    onEvent: (DiaryMoreEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                plantName = state.plantName,
                sortOrder = state.sortOrder,
                onEvent = onEvent
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        if (state.diaries.isEmpty() && !state.isLoading) {
            ListEmptyView(
                plantName = state.plantName,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        } else {
            DiaryList(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DiaryList(
    state: DiaryMoreState,
    onEvent: (DiaryMoreEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoading) {
            onEvent(DiaryMoreEvent.LoadMore)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(
            items = state.diaries,
            key = { item ->
                when (item) {
                    is DiaryMoreListItem.MonthHeader -> "header_${item.yearMonth}"
                    is DiaryMoreListItem.DiaryItem -> "diary_${item.diary.id}"
                }
            }
        ) { item ->
            when (item) {
                is DiaryMoreListItem.MonthHeader -> {
                    MonthHeader(yearMonth = item.yearMonth)
                }

                is DiaryMoreListItem.DiaryItem -> {
                    DiaryListItem(
                        diary = item.diary,
                        onEvent = { event ->
                            if (event is DiaryListEvent.OnDiaryClicked) {
                                onEvent(DiaryMoreEvent.OnDiaryClicked(event.diaryId))
                            }
                        }
                    )
                }
            }
        }

        if (state.isLoading) {
            item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SortButton(
    currentSortOrder: DiaryListSortOrder,
    onSortOrderChanged: (DiaryListSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    @Composable
    fun getSortText(order: DiaryListSortOrder): String {
        return when (order) {
            DiaryListSortOrder.CREATED_LATEST -> stringResource(R.string.sort_diary_created_latest)
            DiaryListSortOrder.CREATED_EARLIEST -> stringResource(R.string.sort_diary_created_earliest)
        }
    }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(8.dp)
        ) {
            Text(
                text = getSortText(currentSortOrder),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(4.dp))
            Image(
                imageVector = IconPack.CaretDown,
                contentDescription = stringResource(R.string.sort)
            )
        }

        AppDropDownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = DiaryListSortOrder.entries,
            selectedItem = currentSortOrder,
            itemText = { getSortText(it) },
            onItemClick = { onSortOrderChanged(it) }
        )
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth) {
    Text(
        text = "${yearMonth.year}.${yearMonth.monthValue}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ListEmptyView(
    plantName: String,
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
            text = if (plantName.isNotEmpty()) {
                stringResource(R.string.diary_list_empty_with_plantName, plantName)
            } else {
                stringResource(R.string.diary_list_empty)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewContent() {
    val now = LocalDate.now()
    GardenLogTheme {
        Content(
            state = DiaryMoreState(
                plantName = "몬스테라",
                diaries = buildDiaryListItems(
                    listOf(
                        DiaryListItemUiModel(
                            id = 1,
                            date = now,
                            content = "오늘 물을 주었다.",
                            imagePath = null
                        ),
                        DiaryListItemUiModel(
                            id = 2,
                            date = now.minusDays(5),
                            content = "새 잎이 나왔다.",
                            imagePath = null
                        ),
                        DiaryListItemUiModel(
                            id = 3,
                            date = now.minusMonths(1),
                            content = "분갈이를 했다.",
                            imagePath = null
                        )
                    )
                ),
                hasMore = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Empty - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmpty() {
    GardenLogTheme {
        Content(
            state = DiaryMoreState(plantName = "몬스테라"),
            onEvent = {}
        )
    }
}
