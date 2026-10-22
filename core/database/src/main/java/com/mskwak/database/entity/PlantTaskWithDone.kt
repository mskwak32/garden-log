package com.mskwak.database.entity

import androidx.room.Embedded
import java.time.LocalDate

data class PlantTaskWithDone(
    @Embedded val task: PlantTaskEntity,
    val latestDoneDate: LocalDate?,
    val isDoneToday: Boolean
)
