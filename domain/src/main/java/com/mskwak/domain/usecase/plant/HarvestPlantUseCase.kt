package com.mskwak.domain.usecase.plant

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.task.SetPlantTaskAlarmUseCase
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase
import java.time.LocalDate

class HarvestPlantUseCase(
    private val plantRepository: PlantRepository,
    private val plantTaskRepository: PlantTaskRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    /**
     * 식물을 수확 처리한다.
     * - DB에 harvestDate, harvestMemo 저장
     * - 물리적 알람만 취소 (DB의 물주기/할일 알람 활성 상태는 변경하지 않음)
     *   → 수확 취소 시 이전 알람 상태를 그대로 복원할 수 있도록
     */
    suspend fun harvest(plantId: Int, harvestMemo: String?, harvestDate: LocalDate) {
        plantRepository.updateHarvestStatus(plantId, harvestDate, harvestMemo)
        setWateringAlarmUseCase(plantId, false)
        plantTaskRepository.getTaskIdsByPlantId(plantId).forEach {
            setPlantTaskAlarmUseCase(it, false)
        }
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
        plantTaskRepository.getTaskIdsByPlantId(plantId).forEach { taskId ->
            if (plantTaskRepository.getTask(taskId)?.alarm?.isActive == true) {
                setPlantTaskAlarmUseCase(taskId, true)
            }
        }
    }
}
