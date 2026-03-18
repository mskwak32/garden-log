package com.mskwak.plant.plant_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.CaretDown
import com.mskwak.common_ui.ui_component.AppDropDownMenu
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.plant.R

@Composable
fun PlantListSortFilter(
    modifier: Modifier = Modifier,
    currentOrder: PlantListSortOrder,
    onEvent: (PlantListEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    @Composable
    fun getSortText(order: PlantListSortOrder): String {
        return when (order) {
            PlantListSortOrder.CREATED_LATEST -> stringResource(R.string.sort_plant_created_latest)
            PlantListSortOrder.CREATED_EARLIEST -> stringResource(R.string.sort_plant_created_earliest)
            PlantListSortOrder.WATERING -> stringResource(R.string.sort_plant_watering_upcoming)
        }
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(8.dp)
        ) {
            Text(
                text = getSortText(currentOrder),
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
            items = PlantListSortOrder.entries,
            selectedItem = currentOrder,
            itemText = { getSortText(it) },
            onItemClick = {
                onEvent(PlantListEvent.OnSortChanged(it))
            }
        )
    }
}