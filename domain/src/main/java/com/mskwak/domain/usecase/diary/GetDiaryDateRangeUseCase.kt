package com.mskwak.domain.usecase.diary

import com.mskwak.domain.repository.DiaryRepository
import java.time.LocalDate

class GetDiaryDateRangeUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(plantId: Int): Pair<LocalDate, LocalDate>? {
        return diaryRepository.getDiaryDateRange(plantId)
    }
}
