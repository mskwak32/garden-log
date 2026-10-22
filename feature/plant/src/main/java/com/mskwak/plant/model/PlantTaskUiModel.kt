package com.mskwak.plant.model

import androidx.compose.runtime.Immutable
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class PlantTaskUiModel(
    val id: Int,
    val title: String,
    val content: String?,
    val type: PlantTaskType,
    // 양수 미래, 0 오늘, 음수 지남
    val remainDays: Int,
    val latestDoneDate: LocalDate?,
    val dueDate: LocalDate,
    val periodDays: Int?,
    val alarmTime: LocalTime,
    val isAlarmActive: Boolean,
    val isDoneToday: Boolean
) {
    val isOverdue: Boolean get() = remainDays < 0
}

fun PlantTask.toPlantTaskUiModel(dueDate: LocalDate, remainDays: Int) = PlantTaskUiModel(
    id = id,
    title = title,
    content = content,
    type = type,
    remainDays = remainDays,
    latestDoneDate = latestDoneDate,
    dueDate = dueDate,
    periodDays = periodDays,
    alarmTime = alarm.time,
    isAlarmActive = alarm.isActive,
    isDoneToday = isDoneToday
)
