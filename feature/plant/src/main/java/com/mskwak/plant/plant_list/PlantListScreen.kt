package com.mskwak.plant.plant_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskwak.design.IconPack
import com.mskwak.design.icon.AddWhite
import com.mskwak.design.icon.CaretDown
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.plant.R
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
                is PlantListEffect.Navigation.ToAddPlant -> {

                }

                is PlantListEffect.Navigation.ToPlantDetail -> {

                }
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
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    SortMenu(
                        currentOrder = state.sortOrder,
                        onEvent = onEvent
                    )
                }
            )
        },
        floatingActionButton = {
            if (state.plants.isNotEmpty()) {
                AddPlantButton(onEvent = onEvent)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.plants.isEmpty()) {
            EmptyList(
                modifier = Modifier.padding(innerPadding),
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

@Composable
fun SortMenu(
    modifier: Modifier = Modifier,
    currentOrder: PlantListSortOrder,
    onEvent: (PlantListEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortLabels = stringArrayResource(id = R.array.home_sort_array)

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(8.dp)
        ) {
            Text(
                text = sortLabels.getOrElse(currentOrder.ordinal) {
                    stringResource(R.string.sort)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.width(4.dp))
            Image(
                imageVector = IconPack.CaretDown,
                contentDescription = stringResource(R.string.sort)
            )
        }

        // 2. 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(10.dp),
            containerColor = Color.White,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
        ) {
            PlantListSortOrder.entries.forEachIndexed { index, order ->
                DropdownMenuItem(
                    text = {
                        Text(text = sortLabels.getOrElse(index) { order.name })
                    },
                    onClick = {
                        onEvent(PlantListEvent.OnSortChanged(order))
                        expanded = false
                    },
                    // 현재 선택된 항목 강조
                    colors = if (order == currentOrder) {
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MenuDefaults.itemColors()
                    }
                )
            }
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
            text = stringResource(R.string.plant_list_empty),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

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

@Preview(showBackground = true)
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
