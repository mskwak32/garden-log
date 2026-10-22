package com.mskwak.domain.usecase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.task.SetPlantTaskAlarmUseCase
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase

class DeletePlantUseCase(
    private val plantRepository: PlantRepository,
    private val diaryRepository: DiaryRepository,
    private val pictureRepository: PictureRepository,
    private val plantTaskRepository: PlantTaskRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        // 삭제 후에는 할일 ID를 조회할 수 없으므로 알람을 먼저 취소
        plantTaskRepository.getTaskIdsByPlantId(plant.id).forEach {
            setPlantTaskAlarmUseCase(it, false)
        }
        plantTaskRepository.deleteByPlantId(plant.id)
        diaryRepository.deleteDiariesByPlantId(plant.id)
        plant.picture?.let { pictureRepository.deletePicture(it) }
        plantRepository.deletePlant(plant)
        setWateringAlarmUseCase(plant.id, isActive = false)
    }
}
