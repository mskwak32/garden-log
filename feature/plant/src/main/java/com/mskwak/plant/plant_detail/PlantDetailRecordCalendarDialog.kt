package com.mskwak.plant.plant_detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.ArrowBackIosBlack
import com.mskwak.common_ui.icon.ArrowForwardIosBlack
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.theme.SkyBlue
import com.mskwak.common_ui.theme.TaskBrown
import com.mskwak.common_ui.theme.TaskBrownLight
import com.mskwak.common_ui.theme.TaskBrownText
import com.mskwak.common_ui.theme.WateringBlue
import com.mskwak.common_ui.theme.WateringBlueText
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.plant.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 물주기·할일 공용 기록 달력. [PlantDetailState.calendarTarget]에 따라 표시 이력과 저장 동작이 바뀐다.
 */
@Composable
fun PlantDetailRecordCalendarDialog(
    state: PlantDetailState,
    onEvent: (PlantDetailEvent) -> Unit
) {
    val busy = state.isCalendarSaving
    AlertDialog(
        // DB 저장 결과가 정해질 때까지 닫기와 선택 변경을 막는다.
        onDismissRequest = {
            if (!busy) onEvent(PlantDetailEvent.OnCalendarClosed)
        },
        text = { CalendarContent(state, onEvent) },
        confirmButton = { SaveButton(state, onEvent) },
        dismissButton = { DismissButton(state, onEvent) }
    )
}

@Composable
private fun CalendarContent(state: PlantDetailState, onEvent: (PlantDetailEvent) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        MonthHeader(state, onEvent)
        WeekdayHeader()
        CalendarGrid(state, onEvent)
        RecordStatus(state, onEvent)
    }
}

@Composable
private fun DismissButton(state: PlantDetailState, onEvent: (PlantDetailEvent) -> Unit) {
    TextButton(
        enabled = !state.isCalendarSaving,
        onClick = { onEvent(PlantDetailEvent.OnCalendarClosed) }
    ) {
        Text(stringResource(if (state.isHarvested) R.string.close else R.string.cancel))
    }
}

@Composable
private fun MonthHeader(state: PlantDetailState, onEvent: (PlantDetailEvent) -> Unit) {
    val arrowColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val today = state.today
    // 조회 범위에는 기존 범위 밖 이력과 수확일도 포함한다.
    val rangeDates = if (state.calendarTarget is CalendarTarget.Task) {
        listOfNotNull(state.createdAt, today) + state.taskLogDates
    } else {
        listOfNotNull(state.createdAt, state.harvestDate, today) +
            state.wateringDates + state.plantTaskLogs.keys
    }
    val first = rangeDates.min()
    val last = rangeDates.max()
    val busy = state.isCalendarSaving
    val locale = Locale.getDefault()
    val previous = stringResource(R.string.watering_previous_month)
    val next = stringResource(R.string.watering_next_month)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            enabled = !busy && state.calendarMonth > YearMonth.from(first),
            onClick = {
                onEvent(PlantDetailEvent.OnCalendarMonthChanged(state.calendarMonth.minusMonths(1)))
            }
        ) {
            Icon(
                imageVector = IconPack.ArrowBackIosBlack,
                contentDescription = previous,
                tint = arrowColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            state.calendarMonth.format(
                DateTimeFormatter.ofPattern(
                    stringResource(R.string.watering_month_format),
                    locale
                )
            )
        )
        TextButton(
            enabled = !busy && state.calendarMonth < YearMonth.from(last),
            onClick = {
                onEvent(PlantDetailEvent.OnCalendarMonthChanged(state.calendarMonth.plusMonths(1)))
            }
        ) {
            Icon(
                imageVector = IconPack.ArrowForwardIosBlack,
                contentDescription = next,
                tint = arrowColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val locale = Locale.getDefault()
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
}

@Composable
private fun CalendarGrid(state: PlantDetailState, onEvent: (PlantDetailEvent) -> Unit) {
    val today = state.today
    val busy = state.isCalendarSaving
    val isTaskMode = state.calendarTarget is CalendarTarget.Task
    // 일요일부터 시작하는 그리드에 월 첫날 앞의 빈 칸을 맞춘다.
    val offset = state.calendarMonth.atDay(1).dayOfWeek.value % 7
    val count = state.calendarMonth.lengthOfMonth()
    repeat((offset + count + 6) / 7) { week ->
        Row(Modifier.fillMaxWidth()) {
            repeat(7) { day ->
                val number = week * 7 + day - offset + 1
                Box(
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (number in 1..count) {
                        val date = state.calendarMonth.atDay(number)
                        val recorded = date in state.calendarRecordDates
                        // 물주기 달력은 그날의 할일 완료 이력을 함께 보여준다.
                        val taskCount = if (isTaskMode) {
                            if (recorded) 1 else 0
                        } else {
                            state.plantTaskLogs[date]?.size ?: 0
                        }
                        val chosen = date == state.calendarSelectedDate
                        // 기존 범위 밖 기록은 취소를 위해 선택하고, 수확 식물은 조회만 허용한다.
                        val selectable = !busy && !state.isCalendarLoading && (
                            state.isHarvested || recorded || taskCount > 0 ||
                                state.createdAt?.let { date in it..today } == true
                            )
                        DateCell(
                            date = date,
                            hasWatering = !isTaskMode && recorded,
                            taskCount = taskCount,
                            chosen = chosen,
                            selectable = selectable,
                            onSelect = { onEvent(PlantDetailEvent.OnCalendarDateSelected(date)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    hasWatering: Boolean,
    taskCount: Int,
    chosen: Boolean,
    selectable: Boolean,
    onSelect: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val blue = if (isDark) SkyBlue else WateringBlue
    val brown = if (isDark) TaskBrownLight else TaskBrown
    val wateringDescription = if (hasWatering) {
        stringResource(R.string.watering_record_accessibility, date.toString())
    } else {
        date.toString()
    }
    val description = if (taskCount > 0) {
        stringResource(R.string.task_record_accessibility, wateringDescription, taskCount)
    } else {
        wateringDescription
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
                onSelect()
            }
            .semantics {
                selected = chosen
                contentDescription = description
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (selectable || chosen) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        // 점이 없어도 같은 높이를 유지해 날짜 숫자 위치가 흔들리지 않게 한다.
        Row(
            modifier = Modifier.height(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (hasWatering) Box(Modifier.size(4.dp).background(blue, CircleShape))
            if (taskCount > 0) Box(Modifier.size(4.dp).background(brown, CircleShape))
        }
    }
}

@Composable
private fun ColumnScope.RecordStatus(
    state: PlantDetailState,
    onEvent: (PlantDetailEvent) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val blue = if (isDark) SkyBlue else WateringBlue
    val blueText = if (isDark) SkyBlue else WateringBlueText
    val brown = if (isDark) TaskBrownLight else TaskBrown
    val brownText = if (isDark) TaskBrownLight else TaskBrownText
    val selectedDate = state.calendarSelectedDate
    val target = state.calendarTarget

    if (state.isCalendarLoading) {
        CircularProgressIndicator(
            Modifier.align(Alignment.CenterHorizontally).size(24.dp)
        )
    } else {
        // 선택 날짜의 실제 이력만 표시한다.
        val titles = if (target is CalendarTarget.Task) {
            listOfNotNull(target.title.takeIf { selectedDate in state.taskLogDates })
        } else {
            if (selectedDate in state.wateringDates) {
                RecordLabel(text = stringResource(R.string.watering), dotColor = blue, textColor = blueText)
            }
            state.plantTaskLogs[selectedDate].orEmpty()
        }
        titles.forEach { title ->
            RecordLabel(text = title, dotColor = brown, textColor = brownText)
        }
    }

    if (state.isCalendarLoadFailed) {
        Text(
            text = stringResource(
                if (target is CalendarTarget.Task) R.string.message_task_load_failed
                else R.string.message_watering_load_failed
            ),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextButton(onClick = { onEvent(PlantDetailEvent.OnCalendarRetry) }) {
            Text(stringResource(R.string.watering_retry))
        }
    } else {
        state.calendarError?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RecordLabel(text: String, dotColor: Color, textColor: Color) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, color = textColor)
    }
}

@Composable
private fun SaveButton(state: PlantDetailState, onEvent: (PlantDetailEvent) -> Unit) {
    val busy = state.isCalendarSaving
    val selectedRecorded = state.calendarSelectedDate in state.calendarRecordDates
    val canAdd = state.createdAt?.let {
        state.calendarSelectedDate in it..state.today
    } == true
    val isTaskMode = state.calendarTarget is CalendarTarget.Task
    // 수확 식물에는 조회와 닫기만 제공하고 기록 변경 동선은 숨긴다.
    if (!state.isHarvested) {
        TextButton(
            enabled = !busy && !state.isCalendarLoading && !state.isCalendarLoadFailed &&
                (selectedRecorded || canAdd),
            onClick = { onEvent(PlantDetailEvent.OnCalendarSaveClicked) }
        ) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(18.dp))
            } else {
                Text(
                    stringResource(
                        when {
                            isTaskMode && selectedRecorded -> R.string.task_cancel_complete
                            isTaskMode -> R.string.complete
                            selectedRecorded -> R.string.watering_cancel_record
                            else -> R.string.watering
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun PreviewDialogFrame(state: PlantDetailState) {
    GardenLogTheme {
        // 별도 창 없이 실제 다이얼로그의 내부 컴포넌트를 직접 렌더링
        Surface(
            modifier = Modifier.width(360.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.padding(24.dp)) {
                CalendarContent(state, onEvent = {})
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DismissButton(state, onEvent = {})
                    SaveButton(state, onEvent = {})
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreviewWatering() {
    val today = LocalDate.of(2026, 10, 15)
    PreviewDialogFrame(
        PlantDetailState(
            createdAt = today.minusMonths(1),
            calendarTarget = CalendarTarget.Watering,
            calendarMonth = YearMonth.from(today),
            calendarSelectedDate = today,
            wateringDates = setOf(today.minusDays(7), today.minusDays(3), today),
            plantTaskLogs = mapOf(
                today to listOf("비료 주기", "분갈이"),
                today.minusDays(5) to listOf("가지치기")
            ),
            isWateringLoading = false,
            today = today
        )
    )
}

@Composable
@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreviewTask() {
    val today = LocalDate.of(2026, 10, 15)
    PreviewDialogFrame(
        PlantDetailState(
            createdAt = today.minusMonths(1),
            calendarTarget = CalendarTarget.Task(1, "비료 주기", PlantTaskType.REPEAT),
            calendarMonth = YearMonth.from(today),
            calendarSelectedDate = today.minusDays(7),
            taskLogDates = setOf(today.minusDays(14), today.minusDays(7)),
            isTaskLogLoading = false,
            today = today
        )
    )
}
