package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mskwak.database.entity.DiaryPictureCrossRef
import com.mskwak.database.entity.PictureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PictureDao {
    @Insert
    suspend fun insertPicture(picture: PictureEntity): Long

    @Query("DELETE FROM picture WHERE id = :id")
    suspend fun deletePicture(id: Int)

    @Query("SELECT * FROM picture WHERE id = :id")
    suspend fun getPicture(id: Int): PictureEntity?

    @Query("SELECT * FROM picture WHERE id = :id")
    fun getPictureFlow(id: Int): Flow<PictureEntity?>

    @Insert
    suspend fun insertDiaryPictureCrossRef(crossRef: DiaryPictureCrossRef)

    @Query("SELECT pictureId FROM diary_picture WHERE diaryId = :diaryId")
    suspend fun getDiaryPictureIds(diaryId: Int): List<Int>

    @Query("DELETE FROM diary_picture WHERE diaryId = :diaryId")
    suspend fun deleteDiaryPictureCrossRefs(diaryId: Int)

    @Query(
        """
        SELECT picture.* FROM picture
        INNER JOIN diary_picture ON picture.id = diary_picture.pictureId
        WHERE diary_picture.diaryId = :diaryId
        """
    )
    fun getDiaryPictures(diaryId: Int): Flow<List<PictureEntity>>
}
