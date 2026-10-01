package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringLogRepository
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

enum class WateringUpdateResult {
    CHANGED,
    UNCHANGED,
    // DB 변경은 완료되었으므로 저장 실패와 구분해서 안내
    ALARM_FAILED
}

class UpdateWateringLogUseCase(
    private val wateringLogRepository: WateringLogRepository,
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(
        plantId: Int,
        date: LocalDate,
        isWatered: Boolean
    ): WateringUpdateResult {
        // 이미 요청한 상태이면 DB 변경과 불필요한 알람 재예약을 생략
        if (!wateringLogRepository.setWateringLog(plantId, date, isWatered)) {
            return WateringUpdateResult.UNCHANGED
        }
        // 기록 저장과 알람 실패를 구분해 저장 완료 후 재시도를 막음
        try {
            // 기록 변경 후의 최신 계산 기준으로 활성 알람만 재예약
            val plant = plantRepository.getPlant(plantId)
            if (plant?.wateringAlarm?.isActive == true) setWateringAlarmUseCase(plantId, true)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return WateringUpdateResult.ALARM_FAILED
        }
        return WateringUpdateResult.CHANGED
    }
}
