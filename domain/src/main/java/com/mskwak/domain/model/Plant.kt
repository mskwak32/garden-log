package com.mskwak.domain.model

import java.time.LocalDate

data class Plant(
    val id: Int,
    val name: String,
    val createdDate: LocalDate,
    val waterPeriod: Int,
    val lastWateringDate: LocalDate,
    val wateringAlarm: Alarm,
    val picture: Picture?,
    val memo: String?,
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null
) {
    val isHarvested: Boolean get() = harvestDate != null
}

enum class PlantListSortOrder {
    CREATED_LATEST,
    CREATED_EARLIEST,
    WATERING
}