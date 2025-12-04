package com.mskwak.domain.useCase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GetDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(diaryId: Int): Flow<Diary> {
        return diaryRepository.getDiary(diaryId)
    }
}