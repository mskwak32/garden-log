package com.mskwak.plant.plant_detail

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack
import com.mskwak.common_ui.icon.AddWhite
import com.mskwak.common_ui.icon.AlarmBlack
import com.mskwak.common_ui.icon.Calendar
import com.mskwak.common_ui.icon.Check
import com.mskwak.common_ui.icon.EventBlack
import com.mskwak.common_ui.icon.Gardening
import com.mskwak.common_ui.icon.MoreHorizBlack
import com.mskwak.common_ui.icon.Repeat
import com.mskwak.common_ui.theme.GardenLogTheme
import com.mskwak.common_ui.ui_component.Switch
import com.mskwak.common_ui.util.clickableWithoutRipple
import com.mskwak.common_ui.util.toDateString
import com.mskwak.common_ui.util.toTimeString
import com.mskwak.domain.Constants
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.plant.R
import com.mskwak.plant.model.PlantTaskUiModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun PlantDetailTaskCard(
    modifier: Modifier = Modifier,
    task: PlantTaskUiModel,
    onEvent: (PlantDetailEvent) -> Unit
) {
    // 반복성 오늘 완료 상태에서만 취소 버튼을 노출한다.
    val showCancel = task.type == PlantTaskType.REPEAT && task.isDoneToday
    val cancelButtonState = remember { MutableTransitionState(showCancel) }
    LaunchedEffect(showCancel) {
        cancelButtonState.targetState = showCancel
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(15.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(15.dp)
            )
            .padding(16.dp)
    ) {
        TitleRow(task = task, onMoreClick = { onEvent(PlantDetailEvent.OnTaskMoreClicked(task.id)) })

        if (!task.content.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = task.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))
        ScheduleRow(task = task)

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoIcon(IconPack.AlarmBlack)
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.task_alarm_format, task.alarmTime.toTimeString()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = task.isAlarmActive,
                onCheckedChange = { onEvent(PlantDetailEvent.ToggleTaskAlarm(task.id, it)) },
                width = 36.dp
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                    .clickable { onEvent(PlantDetailEvent.OnTaskCalendarClicked(task.id)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Calendar,
                    contentDescription = stringResource(R.string.task_calendar),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            CompleteButton(
                modifier = Modifier.weight(1f),
                onClick = { onEvent(PlantDetailEvent.OnTaskCompleteClicked(task.id)) }
            )
            AnimatedVisibility(
                visibleState = cancelButtonState,
                enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
            ) {
                // padding(start)을 AnimatedVisibility 안으로 넣어야 gap도 함께 shrink됨
                CancelButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = { onEvent(PlantDetailEvent.OnTaskCancelClicked(task.id)) }
                )
            }
        }
    }
}

@Composable
private fun TitleRow(task: PlantTaskUiModel, onMoreClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = IconPack.Gardening,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        // 제목·D-day 묶음이 남은 폭을 차지하고, fill = false라서 짧은 제목 뒤에는 D-day가 바로 붙는다.
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (task.remainDays >= 0) {
                    stringResource(R.string.watering_d_day_minus_format, task.remainDays)
                } else {
                    stringResource(R.string.watering_d_day_plus_format, -task.remainDays)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (task.isOverdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = IconPack.MoreHorizBlack,
                contentDescription = stringResource(R.string.menu),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScheduleRow(task: PlantTaskUiModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val text = when (task.type) {
            PlantTaskType.REPEAT -> {
                InfoIcon(IconPack.Repeat)
                stringResource(
                    R.string.task_repeat_summary_format,
                    task.periodDays ?: 0,
                    lastDoneText(task.latestDoneDate)
                )
            }
            PlantTaskType.ONE_TIME -> {
                InfoIcon(IconPack.EventBlack)
                stringResource(R.string.task_due_date_format, task.dueDate.toDateString())
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 물주기 마지막 날짜 표시와 같은 규칙
@Composable
private fun lastDoneText(latestDoneDate: LocalDate?): String {
    if (latestDoneDate == null) return stringResource(R.string.watering_no_record)
    return when (val daysBetween = ChronoUnit.DAYS.between(latestDoneDate, LocalDate.now())) {
        0L -> stringResource(R.string.today)
        in 1..Constants.LAST_WATERING_TEXT_CRITERION -> stringResource(R.string.days_ago_format, daysBetween)
        else -> latestDoneDate.toDateString()
    }
}

@Composable
private fun InfoIcon(imageVector: ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun CompleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            .clickableWithoutRipple(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconPack.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.complete),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
private fun CancelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(15.dp))
            .clickableWithoutRipple(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.cancel),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun PlantDetailAddTaskButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconPack.AddWhite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.task_add_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun previewTask(
    id: Int,
    title: String,
    type: PlantTaskType,
    remainDays: Int,
    content: String? = null,
    isDoneToday: Boolean = false,
    latestDoneDate: LocalDate? = null,
    isAlarmActive: Boolean = false
) = PlantTaskUiModel(
    id = id,
    title = title,
    content = content,
    type = type,
    remainDays = remainDays,
    latestDoneDate = latestDoneDate,
    dueDate = LocalDate.now().plusDays(remainDays.toLong()),
    periodDays = if (type == PlantTaskType.REPEAT) 7 else null,
    alarmTime = LocalTime.of(9, 0),
    isAlarmActive = isAlarmActive,
    isDoneToday = isDoneToday
)

@Preview(showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewTaskCards() {
    GardenLogTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 반복성 오늘 완료
            PlantDetailTaskCard(
                task = previewTask(
                    id = 1,
                    title = "비료 주기",
                    type = PlantTaskType.REPEAT,
                    remainDays = 7,
                    content = "액체 비료를 물에 희석해서 주기",
                    isDoneToday = true,
                    latestDoneDate = LocalDate.now(),
                    isAlarmActive = true
                ),
                onEvent = {}
            )
            // 1회성 지남
            PlantDetailTaskCard(
                task = previewTask(id = 2, title = "분갈이", type = PlantTaskType.ONE_TIME, remainDays = -3),
                onEvent = {}
            )
            // 긴 제목
            PlantDetailTaskCard(
                task = previewTask(
                    id = 3,
                    title = "잎 뒷면 해충 확인하고 방제약 뿌리기",
                    type = PlantTaskType.REPEAT,
                    remainDays = 2,
                    latestDoneDate = LocalDate.now().minusDays(5)
                ),
                onEvent = {}
            )
            PlantDetailAddTaskButton(onClick = {})
        }
    }
}
