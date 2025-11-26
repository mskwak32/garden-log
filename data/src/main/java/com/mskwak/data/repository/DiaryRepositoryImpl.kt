package com.mskwak.data.repository

import com.mskwak.data.mapper.toDiary
import com.mskwak.data.mapper.toDiaryEntity
import com.mskwak.database.dao.DiaryDao
import com.mskwak.domain.Constant
import com.mskwak.domain.model.Diary
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

internal class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {
    override suspend fun addDiary(diary: Diary) {
        diaryDao.insertDiary(diary.toDiaryEntity())
        Timber.d("add new diary")
    }

    override suspend fun updateDiary(diary: Diary) {
        diaryDao.updateDiary(diary.toDiaryEntity())
        Timber.d("update diary. id= ${diary.id}")
    }

    override suspend fun deleteDiary(diary: Diary) {
        diaryDao.deleteDiary(diary.toDiaryEntity())
        Timber.d("delete diary. id= ${diary.id}")
    }

    override suspend fun deleteDiariesByPlantId(plantId: Int) {
        diaryDao.deleteDiariesByPlantId(plantId)
        Timber.d("delete diaries by plantId= $plantId")
    }

    override fun getDiary(id: Int): Flow<Diary> {
        return diaryDao.getDiary(id).map { it.toDiary() }
    }

    override fun getDiariesByPlantId(
        plantId: Int,
        limit: Int?
    ): Flow<List<Diary>> {
        return diaryDao.getDiariesByPlantId(plantId, limit ?: Constant.PAGE_SIZE).map { list ->
            list.map { it.toDiary() }
        }
    }

    override fun getDiaries(
        year: Int,
        month: Int,
        plantId: Int?
    ): Flow<List<Diary>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        return if (plantId == null) {
            diaryDao.getDiaries(startDate, endDate)
        } else {
            diaryDao.getDiariesByPlantId(plantId, startDate, endDate)
        }.map { list ->
            list.map { it.toDiary() }
        }
    }
}