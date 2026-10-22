package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal fun task(
    type: PlantTaskType = PlantTaskType.REPEAT,
    date: LocalDate,
    periodDays: Int? = 7,
    latestDoneDate: LocalDate? = null,
    plantId: Int = 1,
    time: LocalTime = LocalTime.of(9, 0)
) = PlantTask(
    id = 1,
    plantId = plantId,
    title = "t",
    content = null,
    type = type,
    date = date,
    periodDays = periodDays,
    alarm = Alarm(time, true),
    createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    latestDoneDate = latestDoneDate
)
