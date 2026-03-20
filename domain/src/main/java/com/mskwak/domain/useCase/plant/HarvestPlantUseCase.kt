package com.mskwak.domain.useCase.plant

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.useCase.watering.SetWateringAlarmUseCase
import java.time.LocalDate

class HarvestPlantUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    /**
     * 식물을 수확 처리한다.
     * - DB에 harvestDate=오늘, harvestMemo 저장
     * - 물리적 알람만 취소 (DB의 watering_alarm_isActive는 변경하지 않음)
     *   → 수확 취소 시 이전 알람 상태를 그대로 복원할 수 있도록
     */
    suspend fun harvest(plantId: Int, harvestMemo: String?) {
        plantRepository.updateHarvestStatus(plantId, LocalDate.now(), harvestMemo)
        setWateringAlarmUseCase(plantId, false)
    }

    /**
     * 수확을 취소한다.
     * - DB에 harvestDate=null, harvestMemo=null 저장
     * - DB의 watering_alarm_isActive가 true이면 물리적 알람을 재등록
     */
    suspend fun cancelHarvest(plantId: Int) {
        plantRepository.updateHarvestStatus(plantId, null, null)
        val plant = plantRepository.getPlant(plantId) ?: return
        if (plant.wateringAlarm.isActive) {
            setWateringAlarmUseCase(plantId, true)
        }
    }
}
