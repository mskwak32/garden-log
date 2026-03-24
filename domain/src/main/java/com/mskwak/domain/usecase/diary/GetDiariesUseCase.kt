package com.mskwak.domain.usecase.diary

import com.mskwak.domain.model.Diary
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetDiariesUseCase(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(
        year: Int,
        month: Int,
        sortOder: DiaryListSortOrder,
        plantId: Int? = null
    ): Flow<List<Diary>> {
        return diaryRepository.getDiaries(year, month, plantId).map { list ->
            list.applySort(sortOder)
        }.flowOn(Dispatchers.IO)
    }

    private fun List<Diary>.applySort(sortOder: DiaryListSortOrder): List<Diary> {
        return when (sortOder) {
            DiaryListSortOrder.CREATED_LATEST -> {
                sortedByDescending { it.createdDate }
            }

            DiaryListSortOrder.CREATED_EARLIEST -> {
                sortedBy { it.createdDate }
            }
        }
    }
}