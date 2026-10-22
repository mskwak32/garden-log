package com.mskwak.plant.plant_detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.AlarmBlack
import com.mskwak.common_ui.icon.ArrowForwardIosBlack
import com.mskwak.common_ui.icon.EventBlack
import com.mskwak.common_ui.icon.Repeat
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.util.toDateString
import com.mskwak.common_ui.util.toTimeString
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.plant.R
import com.mskwak.plant.dialog.WateringPeriodDialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun PlantDetailTaskDialog(
    state: PlantTaskDialogState,
    isSaving: Boolean,
    onEvent: (PlantDetailEvent) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        TaskDatePickerDialog(
            initialDate = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onEvent(PlantDetailEvent.OnTaskDateChanged(it))
                showDatePicker = false
            }
        )
    }

    if (showPeriodDialog) {
        WateringPeriodDialog(
            initialPeriod = state.periodDays,
            title = stringResource(R.string.task_period),
            showNone = false,
            onDismiss = { showPeriodDialog = false },
            onConfirm = {
                onEvent(PlantDetailEvent.OnTaskPeriodChanged(it))
                showPeriodDialog = false
            }
        )
    }

    if (showTimePicker) {
        TaskTimePickerDialog(
            initialTime = state.alarmTime,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                onEvent(PlantDetailEvent.OnTaskAlarmTimeChanged(it))
                showTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = { onEvent(PlantDetailEvent.OnTaskDialogDismissed) },
        text = {
            TaskForm(
                state = state,
                onEvent = onEvent,
                onDateClick = { showDatePicker = true },
                onPeriodClick = { showPeriodDialog = true },
                onAlarmTimeClick = { showTimePicker = true }
            )
        },
        confirmButton = {
            TextButton(
                enabled = state.canSave && !isSaving,
                onClick = { onEvent(PlantDetailEvent.OnTaskSaveClicked) }
            ) {
                Text(stringResource(if (state.isEdit) R.string.edit else R.string.task_create))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    onEvent(
                        if (state.isEdit) PlantDetailEvent.OnTaskDeleteClicked
                        else PlantDetailEvent.OnTaskDialogDismissed
                    )
                }
            ) {
                Text(
                    text = stringResource(if (state.isEdit) R.string.delete else R.string.cancel),
                    color = if (state.isEdit) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
        }
    )
}

@Composable
private fun TaskForm(
    state: PlantTaskDialogState,
    onEvent: (PlantDetailEvent) -> Unit,
    onDateClick: () -> Unit,
    onPeriodClick: () -> Unit,
    onAlarmTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TypeSelector(
            selected = state.type,
            // 수정 시에는 종류를 바꿀 수 없다.
            enabled = !state.isEdit,
            onSelect = { onEvent(PlantDetailEvent.OnTaskTypeChanged(it)) }
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(PlantDetailEvent.OnTaskTitleChanged(it)) },
            label = { Text(stringResource(R.string.task_title)) },
            supportingText = {
                Text(
                    text = stringResource(
                        R.string.task_title_counter_format,
                        state.title.length,
                        PlantTaskDialogState.TITLE_MAX_LENGTH
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.content,
            onValueChange = { onEvent(PlantDetailEvent.OnTaskContentChanged(it)) },
            label = { Text(stringResource(R.string.task_content)) },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            SettingRow(
                icon = IconPack.EventBlack,
                label = stringResource(
                    if (state.type == PlantTaskType.REPEAT) R.string.task_start_date else R.string.task_due_date
                ),
                value = state.date.toDateString(),
                onClick = onDateClick
            )
            if (state.type == PlantTaskType.REPEAT) {
                SettingDivider()
                SettingRow(
                    icon = IconPack.Repeat,
                    label = stringResource(R.string.task_period),
                    value = stringResource(R.string.watering_period_unit, state.periodDays),
                    onClick = onPeriodClick
                )
            }
            SettingDivider()
            SettingRow(
                icon = IconPack.AlarmBlack,
                label = stringResource(R.string.watering_alarm),
                value = state.alarmTime.toTimeString(),
                onClick = onAlarmTimeClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selected: PlantTaskType,
    enabled: Boolean,
    onSelect: (PlantTaskType) -> Unit
) {
    val options = listOf(
        PlantTaskType.ONE_TIME to R.string.task_type_one_time,
        PlantTaskType.REPEAT to R.string.task_type_repeat
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(stringResource(label))
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = IconPack.ArrowForwardIosBlack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 44.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    // 예정일·시작일은 과거와 미래를 모두 허용한다.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis == null) {
                    onDismiss()
                } else {
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                }
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.watering_alarm)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreviewCreate() {
    GardenLogTheme {
        PlantDetailTaskDialog(
            state = PlantTaskDialogState(date = LocalDate.of(2026, 10, 22)),
            isSaving = false,
            onEvent = {}
        )
    }
}

@Composable
@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreviewEdit() {
    GardenLogTheme {
        PlantDetailTaskDialog(
            state = PlantTaskDialogState(
                taskId = 1,
                title = "비료 주기",
                content = "액체 비료를 물에 희석해서 주기",
                type = PlantTaskType.REPEAT,
                date = LocalDate.of(2026, 10, 29),
                periodDays = 14
            ),
            isSaving = false,
            onEvent = {}
        )
    }
}
