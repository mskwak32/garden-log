package com.mskwak.plant.plant_list

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.mskwak.common_ui.Screen
import com.mskwak.design.IconPack
import com.mskwak.design.icon.AddWhite
import com.mskwak.design.theme.GardenLogTheme
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.plant.R
import com.mskwak.plant.model.PlantListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate

data object PlantListScreen : Screen

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
                    PlantListSortFilter(
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
