package com.mskwak.plant.model

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