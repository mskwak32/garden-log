package com.mskwak.domain.usecase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository

class AddDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(diary: Diary) {
        diaryRepository.addDiary(diary)
    }
}