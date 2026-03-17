package com.mskwak.plant.model

import androidx.compose.runtime.Immutable
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringDays
import java.time.LocalDate

@Immutable
data class PlantListItemUiModel(
    val plantId: Int,
    val name: String,
    val imagePath: String?,
    val dDay: Int,
    val status: WateringStatus,
    val createdAt: LocalDate
)

fun Plant.toPlantListItemUiModel(
    getWateringDays: (Plant) -> WateringDays
): PlantListItemUiModel {
    val today = LocalDate.now()
    val wateringDays = getWateringDays(this)
    val (status, dDay) = when {
        lastWateringDate == today -> {
            WateringStatus.TODAY_DONE to 0
        }

        wateringDays.isOverDue -> {
            WateringStatus.OVERDUE to wateringDays.days
        }

        wateringDays.days == 0 -> {
            WateringStatus.TODAY to 0
        }

        else -> {
            WateringStatus.UPCOMING to wateringDays.days
        }
    }

    return PlantListItemUiModel(
        plantId = id,
        name = name,
        imagePath = picture?.path,
        dDay = dDay,
        status = status,
        createdAt = createdDate
    )
}
