package com.mskwak.plant.plant_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.theme.SkyBlue
import com.mskwak.common_ui.theme.WateringBlue
import com.mskwak.common_ui.theme.WateringBlueText
import com.mskwak.plant.R
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlantDetailWateringDateDialog(
    state: PlantDetailState,
    onEvent: (PlantDetailEvent) -> Unit
) {
    val today = state.today
    // 조회 범위에는 기존 범위 밖 이력과 수확일도 포함한다.
    val first = minOf(
        state.createdAt ?: today,
        state.wateringDates.minOrNull() ?: today,
        state.harvestDate ?: today
    )
    val last = maxOf(
        today,
        state.wateringDates.maxOrNull() ?: today,
        state.harvestDate ?: today,
        state.createdAt ?: today
    )
    val busy = state.isWateringSaving
    val selectedRecorded = state.selectedWateringDate in state.wateringDates
    val canAdd = state.createdAt?.let {
        state.selectedWateringDate in it..today
    } == true
    val blue = if (isSystemInDarkTheme()) SkyBlue else WateringBlue
    val blueText = if (isSystemInDarkTheme()) SkyBlue else WateringBlueText
    val locale = Locale.getDefault()
    val previous = stringResource(R.string.watering_previous_month)
    val next = stringResource(R.string.watering_next_month)

    AlertDialog(
        // DB 저장 결과가 정해질 때까지 닫기와 선택 변경을 막는다.
        onDismissRequest = {
            if (!busy) onEvent(PlantDetailEvent.OnWateringCalendarClosed)
        },
        title = { Text(stringResource(R.string.watering_calendar)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        enabled = !busy && state.wateringMonth > YearMonth.from(first),
                        onClick = {
                            onEvent(
                                PlantDetailEvent.OnWateringMonthChanged(
                                    state.wateringMonth.minusMonths(1)
                                )
                            )
                        }
                    ) {
                        Text("‹", Modifier.semantics { contentDescription = previous })
                    }
                    Text(
                        state.wateringMonth.format(
                            DateTimeFormatter.ofPattern(
                                stringResource(R.string.watering_month_format),
                                locale
                            )
                        )
                    )
                    TextButton(
                        enabled = !busy && state.wateringMonth < YearMonth.from(last),
                        onClick = {
                            onEvent(
                                PlantDetailEvent.OnWateringMonthChanged(
                                    state.wateringMonth.plusMonths(1)
                                )
                            )
                        }
                    ) {
                        Text("›", Modifier.semantics { contentDescription = next })
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    (0..6).forEach { day ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = DayOfWeek.of(if (day == 0) 7 else day)
                                    .getDisplayName(TextStyle.NARROW, locale),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                // 일요일부터 시작하는 그리드에 월 첫날 앞의 빈 칸을 맞춘다.
                val offset = state.wateringMonth.atDay(1).dayOfWeek.value % 7
                val count = state.wateringMonth.lengthOfMonth()
                repeat((offset + count + 6) / 7) { week ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { day ->
                            val number = week * 7 + day - offset + 1
                            Box(
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (number in 1..count) {
                                    val date = state.wateringMonth.atDay(number)
                                    val recorded = date in state.wateringDates
                                    val chosen = date == state.selectedWateringDate
                                    // 기존 범위 밖 기록은 취소를 위해 선택하고, 수확 식물은 조회만 허용한다.
                                    val selectable = !busy && !state.isWateringLoading && (
                                        state.isHarvested || recorded || state.createdAt?.let {
                                            date in it..today
                                        } == true
                                    )
                                    val description = if (recorded) {
                                        stringResource(
                                            R.string.watering_record_accessibility,
                                            date.toString()
                                        )
                                    } else {
                                        date.toString()
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .background(
                                                if (chosen) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent,
                                                CircleShape
                                            )
                                            .border(
                                                if (chosen) 1.dp else 0.dp,
                                                if (chosen) MaterialTheme.colorScheme.primary
                                                else Color.Transparent,
                                                CircleShape
                                            )
                                            // 배경 모양만 지정하면 리플은 사각형으로 남으므로 같은 모양으로 제한
                                            .clip(CircleShape)
                                            .clickable(enabled = selectable) {
                                                onEvent(PlantDetailEvent.OnWateringDateSelected(date))
                                            }
                                            .semantics {
                                                selected = chosen
                                                contentDescription = description
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = number.toString(),
                                            color = if (selectable || chosen) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            }
                                        )
                                        Box(
                                            Modifier.size(4.dp).background(
                                                if (recorded) blue else Color.Transparent,
                                                CircleShape
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.isWateringLoading) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.CenterHorizontally).size(24.dp)
                    )
                }
                // 선택 날짜의 실제 이력이 있을 때만 물주기 표시를 노출한다.
                if (selectedRecorded && !state.isWateringLoading) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).background(blue, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.watering), color = blueText)
                    }
                }
                state.wateringError?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (state.wateringError == R.string.message_watering_load_failed) {
                    TextButton(onClick = { onEvent(PlantDetailEvent.OnWateringRetry) }) {
                        Text(stringResource(R.string.watering_retry))
                    }
                }
            }
        },
        confirmButton = {
            // 수확 식물에는 조회와 닫기만 제공하고 기록 변경 동선은 숨긴다.
            if (!state.isHarvested) {
                TextButton(
                    enabled = !busy && !state.isWateringLoading &&
                        state.wateringError != R.string.message_watering_load_failed &&
                        (selectedRecorded || canAdd),
                    onClick = { onEvent(PlantDetailEvent.OnWateringDateSaved) }
                ) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(18.dp))
                    } else {
                        Text(
                            stringResource(
                                if (selectedRecorded) R.string.watering_cancel_record
                                else R.string.watering
                            )
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !busy,
                onClick = { onEvent(PlantDetailEvent.OnWateringCalendarClosed) }
            ) {
                Text(stringResource(if (state.isHarvested) R.string.close else R.string.cancel))
            }
        }
    )
}
