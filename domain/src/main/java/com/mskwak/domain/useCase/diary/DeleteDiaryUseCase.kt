package com.mskwak.domain.useCase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository

class DeleteDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val pictureRepository: PictureRepository
) {
    suspend operator fun invoke(diary: Diary) {
        diary.pictureList?.forEach {
            pictureRepository.deletePicture(it)
        }
        diaryRepository.deleteDiary(diary)
    }
}