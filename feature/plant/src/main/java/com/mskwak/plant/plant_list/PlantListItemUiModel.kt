package com.mskwak.plant.plant_list

import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringDays
import java.time.LocalDate

data class PlantListItemUiModel(
    val plantId: Int,
    val name: String,
    val imagePath: String?,
    val dDay: Int,
    val status: WateringStatus,
    val createdAt: LocalDate
)

/**
 * 물주기 ui의 상태를 결정
 * TODAY: 오늘 물을 줘야함
 * TODAY_DONE, 오늘 물을 줌
 * OVERDUE: 물주기가 지난 경우
 * UPCOMING: 다음 물주기까지 아직 남음
 */
enum class WateringStatus {
    TODAY,
    TODAY_DONE,
    OVERDUE,
    UPCOMING
}

fun Plant.toPlantListItemUiModel(
    getWateringDays: (Plant) -> WateringDays
): PlantListItemUiModel {
    val today = LocalDate.now()
    val wateringDays = getWateringDays(this)
    val (status, dDay) = when {
        wateringDays.isOverDue -> {
            WateringStatus.OVERDUE to wateringDays.days
        }

        lastWateringDate == today -> {
            WateringStatus.TODAY_DONE to 0
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
