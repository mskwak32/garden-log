package com.mskwak.domain.usecase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import java.time.LocalDate

class GetDiariesForExportUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Diary> {
        return diaryRepository.getDiariesByPlantIdAndDateRange(plantId, startDate, endDate)
    }
}
