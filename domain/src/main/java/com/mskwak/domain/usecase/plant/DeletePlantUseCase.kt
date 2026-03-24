package com.mskwak.domain.useCase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.useCase.watering.SetWateringAlarmUseCase

class DeletePlantUseCase(
    private val plantRepository: PlantRepository,
    private val diaryRepository: DiaryRepository,
    private val pictureRepository: PictureRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        diaryRepository.deleteDiariesByPlantId(plant.id)
        plant.picture?.let { pictureRepository.deletePicture(it) }
        plantRepository.deletePlant(plant)
        setWateringAlarmUseCase(plant.id, isActive = false)
    }
}