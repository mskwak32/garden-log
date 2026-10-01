package com.mskwak.domain.usecase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import kotlinx.coroutines.CancellationException

class UpdatePlantUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant): WateringUpdateResult {
        plantRepository.updatePlant(plant)
        val id = plant.id
        // 저장 완료 후 알람 실패로 식물 저장을 반복하지 않음
        return try {
            setWateringAlarmUseCase(id, plant.wateringAlarm.isActive)
            WateringUpdateResult.CHANGED
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            WateringUpdateResult.ALARM_FAILED
        }
    }
}
