package com.mskwak.data.repository

import com.mskwak.data.mapper.toDiary
import com.mskwak.data.mapper.toDiaryEntity
import com.mskwak.data.mapper.toPictureEntity
import com.mskwak.database.dao.DiaryDao
import com.mskwak.database.dao.PictureDao
import com.mskwak.database.entity.DiaryPictureCrossRef
import com.mskwak.domain.Constants
import com.mskwak.domain.model.Diary
import com.mskwak.domain.model.Picture
import com.mskwak.domain.repository.DiaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao,
    private val pictureDao: PictureDao
) : DiaryRepository {

    override suspend fun addDiary(diary: Diary) {
        val diaryId = diaryDao.insertDiary(diary.toDiaryEntity()).toInt()
        insertDiaryPictures(diaryId, diary.pictureList)
        Timber.d("add new diary id=$diaryId")
    }

    override suspend fun updateDiary(diary: Diary) {
        diaryDao.updateDiary(diary.toDiaryEntity())
        replaceDiaryPictures(diary.id, diary.pictureList)
        Timber.d("update diary. id=${diary.id}")
    }

    override suspend fun deleteDiary(diary: Diary) {
        deleteAllDiaryPictures(diary.id)
        diaryDao.deleteDiary(diary.toDiaryEntity())
        Timber.d("delete diary. id=${diary.id}")
    }

    override suspend fun deleteDiariesByPlantId(plantId: Int) {
        val diaryIds: List<Int> = diaryDao.getDiaryIdsByPlantId(plantId)
        for (diaryId in diaryIds) deleteAllDiaryPictures(diaryId)
        diaryDao.deleteDiariesByPlantId(plantId)
        Timber.d("delete diaries by plantId=$plantId")
    }

    override fun getDiary(id: Int): Flow<Diary> {
        return combine(
            diaryDao.getDiary(id),
            pictureDao.getDiaryPictures(id)
        ) { diary, pictures ->
            diary.toDiary(pictures)
        }
    }

    override fun getDiariesByPlantId(plantId: Int, limit: Int?): Flow<List<Diary>> {
        return diaryDao.getDiariesByPlantId(plantId, limit ?: Constants.PAGE_SIZE)
            .flatMapLatest { diaries ->
                if (diaries.isEmpty()) return@flatMapLatest flowOf(emptyList())
                combine(diaries.map { diary ->
                    pictureDao.getDiaryPictures(diary.id).map { pictures ->
                        diary.toDiary(pictures)
                    }
                }) { it.toList() }
            }
    }

    override fun getDiaries(year: Int, month: Int, plantId: Int?): Flow<List<Diary>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        val diariesFlow = if (plantId == null) {
            diaryDao.getDiaries(startDate, endDate)
        } else {
            diaryDao.getDiariesByPlantId(plantId, startDate, endDate)
        }

        return diariesFlow.flatMapLatest { diaries ->
            if (diaries.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(diaries.map { diary ->
                pictureDao.getDiaryPictures(diary.id).map { pictures ->
                    diary.toDiary(pictures)
                }
            }) { it.toList() }
        }
    }

    private suspend fun insertDiaryPictures(diaryId: Int, pictures: List<Picture>?) {
        pictures?.forEach { picture ->
            val pictureId = pictureDao.insertPicture(picture.toPictureEntity()).toInt()
            pictureDao.insertDiaryPictureCrossRef(DiaryPictureCrossRef(diaryId, pictureId))
        }
    }

    private suspend fun replaceDiaryPictures(diaryId: Int, pictures: List<Picture>?) {
        deleteAllDiaryPictures(diaryId)
        insertDiaryPictures(diaryId, pictures)
    }

    private suspend fun deleteAllDiaryPictures(diaryId: Int) {
        val pictureIds: List<Int> = pictureDao.getDiaryPictureIds(diaryId)
        pictureDao.deleteDiaryPictureCrossRefs(diaryId)
        for (pictureId in pictureIds) pictureDao.deletePicture(pictureId)
    }
}
