package com.mskwak.domain.usecase.diary

import com.mskwak.domain.Constants
import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetDiariesByPlantIdUseCase(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(
        plantId: Int,
        limit: Int = Constants.MAX_DIARY_SIZE_ON_PLANT_DETAIL,
        page: Int = 0,
        ascending: Boolean = false
    ): Flow<List<Diary>> {
        return diaryRepository.getDiariesByPlantId(
            plantId = plantId,
            limit = limit,
            offset = page * limit,
            ascending = ascending
        )
    }
}