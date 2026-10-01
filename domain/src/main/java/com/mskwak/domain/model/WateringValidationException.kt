package com.mskwak.domain.model

enum class WateringValidationReason {
    OUTSIDE_RANGE, HARVESTED, FUTURE_CREATED_DATE, CREATED_AFTER_WATERING_DATE
}

// 데이터 계층의 검증 이유를 전달하고, 사용자 안내 문구는 UI 계층에서 선택
class WateringValidationException(val reason: WateringValidationReason) :
    IllegalArgumentException(reason.name)
