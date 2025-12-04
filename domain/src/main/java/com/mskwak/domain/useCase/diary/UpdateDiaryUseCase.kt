package com.mskwak.domain.useCase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository

class UpdateDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(diary: Diary) {
        diaryRepository.updateDiary(diary)
    }
}