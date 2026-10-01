package com.mskwak.domain.model

import java.time.LocalDate

data class Plant(
    val id: Int,
    val name: String,
    val createdDate: LocalDate,
    val waterPeriod: Int,
    // D-day·알람 계산 기준. 실제 이력이 없으면 조회 시 심은 날짜를 사용
    val lastWateringDate: LocalDate,
    val wateringAlarm: Alarm,
    val picture: Picture?,
    val memo: String?,
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null,
    // 실제 최신 이력 날짜. null이면 화면에 기록 없음 표시
    val latestWateringDate: LocalDate? = lastWateringDate,
    // 계산 기준이 오늘이어도 실제 오늘 기록이 없으면 완료로 처리하지 않음
    val isWateredToday: Boolean = latestWateringDate == LocalDate.now()
) {
    val isHarvested: Boolean get() = harvestDate != null
}

enum class PlantListSortOrder {
    CREATED_LATEST,
    CREATED_EARLIEST,
    WATERING
}