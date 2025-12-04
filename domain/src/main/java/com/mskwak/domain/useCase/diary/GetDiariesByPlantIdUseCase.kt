package com.mskwak.domain.useCase.diary

import com.mskwak.domain.Constant
import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetDiariesByPlantIdUseCase(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(plantId: Int): Flow<List<Diary>> {
        return diaryRepository.getDiariesByPlantId(plantId, Constant.MAX_DIARY_SIZE_ON_PLANT_DETAIL)
    }
}