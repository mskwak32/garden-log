package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.WateringLogRepository

class GetWateringDatesUseCase(private val wateringLogRepository: WateringLogRepository) {
    // 전체 이력을 구독해 월 이동 시 재조회 없이 기존 범위 밖 기록도 표시
    operator fun invoke(plantId: Int) = wateringLogRepository.observeWateringDates(plantId)
}
