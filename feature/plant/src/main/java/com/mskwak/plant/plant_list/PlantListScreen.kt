package com.mskwak.plant.plant_list

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.AddWhite
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.LocalNavBottomBarPadding
import com.mskwak.common_ui.util.clickableWithoutRipple
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.plant.R
import com.mskwak.plant.model.PlantListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate

@Composable
fun PlantListScreen(
    viewModel: PlantListViewModel = hiltViewModel(),
    navigate: (PlantListEffect.Navigation) -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    Content(
        state = state,
        onEvent = viewModel::setEvent
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlantListEffect.Navigation -> navigate(effect)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: PlantListState,
    onEvent: (PlantListEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(state = state, onEvent = onEvent)
        },
        floatingActionButton = {
            // 수확 탭에서는 FAB 숨김
            if (state.plants.isNotEmpty() && state.selectedTab == PlantListTab.MY_GARDEN) {
                AddPlantButton(onEvent = onEvent)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            .union(WindowInsets(bottom = LocalNavBottomBarPadding.current))
    ) { innerPadding ->
        if (state.plants.isEmpty()) {
            EmptyList(
                modifier = Modifier.padding(innerPadding),
                isHarvestedTab = state.selectedTab == PlantListTab.HARVESTED,
                onEvent = onEvent
            )
        } else {
            PlantList(
                modifier = Modifier.padding(innerPadding),
                uiModels = state.plants,
                onEvent = onEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: PlantListState,
    onEvent: (PlantListEvent) -> Unit
) {
    TopAppBar(
        modifier = Modifier.padding(horizontal = 8.dp),
        title = {
            TabTitle(
                selectedTab = state.selectedTab,
                onTabChanged = { onEvent(PlantListEvent.OnTabChanged(it)) }
            )
        },
        actions = {
            // 수확 탭에서는 정렬 불필요 (물주기 기반 정렬이 의미 없음)
            if (state.selectedTab == PlantListTab.MY_GARDEN) {
                PlantListSortFilter(
                    currentOrder = state.sortOrder,
                    onEvent = onEvent
                )
            }
        }
    )
}

@Composable
private fun TabTitle(
    selectedTab: PlantListTab,
    onTabChanged: (PlantListTab) -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlantListTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Text(
                text = stringResource(
                    when (tab) {
                        PlantListTab.MY_GARDEN -> R.string.tab_my_garden
                        PlantListTab.HARVESTED -> R.string.tab_harvested
                    }
                ),
                style = if (isSelected) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                },
                modifier = Modifier.clickableWithoutRipple { onTabChanged(tab) }
            )
        }
    }
}

@Composable
private fun AddPlantButton(
    onEvent: (PlantListEvent) -> Unit
) {
    FloatingActionButton(
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.secondary,
        onClick = { onEvent(PlantListEvent.AddPlant) }
    ) {
        Image(
            imageVector = IconPack.AddWhite,
            contentDescription = stringResource(R.string.new_plant),
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun PlantList(
    modifier: Modifier = Modifier,
    uiModels: List<PlantListItemUiModel>,
    onEvent: (PlantListEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = uiModels,
            key = { it.plantId }
        ) { item ->
            PlantListItem(
                uiModel = item,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun EmptyList(
    modifier: Modifier = Modifier,
    isHarvestedTab: Boolean = false,
    onEvent: (PlantListEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_flower_pot),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(
                if (isHarvestedTab) R.string.harvested_list_empty else R.string.plant_list_empty
            ),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!isHarvestedTab) {
            Spacer(Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = { onEvent(PlantListEvent.AddPlant) }
            ) {
                Text(
                    text = stringResource(R.string.new_plant),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val uiModels = listOf(
        PlantListItemUiModel(
            plantId = 1,
            name = "Monstera",
            imagePath = null,
            dDay = 1,
            status = WateringStatus.OVERDUE,
            createdAt = LocalDate.now()
        ),
        PlantListItemUiModel(
            plantId = 2,
            name = "Monstera",
            imagePath = null,
            dDay = 1,
            status = WateringStatus.UPCOMING,
            createdAt = LocalDate.now()
        )
    )
    GardenLogTheme {
        Content(
            state = PlantListState(
                plants = uiModels,
                sortOrder = PlantListSortOrder.CREATED_LATEST
            ),
            onEvent = {}
        )
    }
}
