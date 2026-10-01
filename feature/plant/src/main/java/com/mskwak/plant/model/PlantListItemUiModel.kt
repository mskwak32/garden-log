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
    val createdAt: LocalDate,
    val harvestDate: LocalDate? = null
) {
    val isHarvested: Boolean get() = harvestDate != null
}

fun Plant.toPlantListItemUiModel(
    getWateringDays: (Plant) -> WateringDays
): PlantListItemUiModel {
    val wateringDays = getWateringDays(this)
    val (status, dDay) = when {
        // 심은 날짜 대체값이나 미래 이력으로 오늘 완료를 추정하지 않는다.
        isWateredToday -> {
            WateringStatus.TODAY_DONE to wateringDays.days
        }

        !wateringDays.hasPeriod -> {
            WateringStatus.NO_PERIOD to wateringDays.days
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
        createdAt = createdDate,
        harvestDate = harvestDate
    )
}
