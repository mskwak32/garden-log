package com.mskwak.plant.diary_list

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.CaretDown
import com.mskwak.common_ui.theme.PreviewTheme
import com.mskwak.common_ui.ui_component.AppDropDownMenu
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.plant.R

@Composable
fun DiaryListSortFilter(
    modifier: Modifier = Modifier,
    currentSortOrder: DiaryListSortOrder,
    onEvent: (DiaryListEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    @Composable
    fun getSortText(order: DiaryListSortOrder): String {
        return when (order) {
            DiaryListSortOrder.CREATED_LATEST -> stringResource(R.string.sort_diary_created_latest)
            DiaryListSortOrder.CREATED_EARLIEST -> stringResource(R.string.sort_diary_created_earliest)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
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
                onItemClick = {
                    onEvent(DiaryListEvent.SortOderChanged(it))
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiaryListSortFilterPreview() {
    PreviewTheme {
        DiaryListSortFilter(
            currentSortOrder = DiaryListSortOrder.CREATED_LATEST,
            onEvent = {}
        )
    }
}
