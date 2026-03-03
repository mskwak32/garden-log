package com.mskwak.database.migration

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import java.io.File

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ---------------------------------------------------------
        // 1. picture 테이블 생성
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `picture` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `path` TEXT NOT NULL,
                `fileName` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """
        )

        // ---------------------------------------------------------
        // 2. diary_picture 중간 테이블 생성
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `diary_picture` (
                `diaryId` INTEGER NOT NULL,
                `pictureId` INTEGER NOT NULL,
                PRIMARY KEY(`diaryId`, `pictureId`)
            )
            """
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_picture_diaryId` ON `diary_picture` (`diaryId`)")

        // ---------------------------------------------------------
        // 3. plant 테이블 마이그레이션
        //    기존: picture_path TEXT, picture_fileName TEXT, picture_createdAt INTEGER (Embedded)
        //    변경: pictureId INTEGER (picture 테이블 FK)
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `plant_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `createdDate` TEXT NOT NULL,
                `waterPeriod` INTEGER NOT NULL,
                `lastWateringDate` TEXT NOT NULL,
                `watering_alarm_time` TEXT NOT NULL,
                `watering_alarm_isActive` INTEGER NOT NULL,
                `pictureId` INTEGER,
                `memo` TEXT
            )
            """
        )

        val plantCursor = db.query("SELECT * FROM plant")
        if (plantCursor.moveToFirst()) {
            do {
                val values = ContentValues()
                values.put("id", plantCursor.getInt(plantCursor.getColumnIndexOrThrow("id")))
                values.put("name", plantCursor.getString(plantCursor.getColumnIndexOrThrow("name")))
                values.put("createdDate", plantCursor.getString(plantCursor.getColumnIndexOrThrow("createdDate")))
                values.put("waterPeriod", plantCursor.getInt(plantCursor.getColumnIndexOrThrow("waterPeriod")))
                values.put("lastWateringDate", plantCursor.getString(plantCursor.getColumnIndexOrThrow("lastWateringDate")))
                values.put("watering_alarm_time", plantCursor.getString(plantCursor.getColumnIndexOrThrow("watering_alarm_time")))
                values.put("watering_alarm_isActive", plantCursor.getInt(plantCursor.getColumnIndexOrThrow("watering_alarm_isActive")))

                val memoIndex = plantCursor.getColumnIndexOrThrow("memo")
                if (plantCursor.isNull(memoIndex)) values.putNull("memo")
                else values.put("memo", plantCursor.getString(memoIndex))

                // 기존 embedded 사진이 있으면 picture 테이블에 삽입 후 pictureId 연결
                val pathIndex = plantCursor.getColumnIndexOrThrow("picture_path")
                if (!plantCursor.isNull(pathIndex)) {
                    val path = plantCursor.getString(pathIndex)
                    val fileName = plantCursor.getString(plantCursor.getColumnIndexOrThrow("picture_fileName"))
                    val createdAt = plantCursor.getLong(plantCursor.getColumnIndexOrThrow("picture_createdAt"))

                    val picValues = ContentValues()
                    picValues.put("path", path)
                    picValues.put("fileName", fileName)
                    picValues.put("createdAt", createdAt)

                    val pictureId = db.insert("picture", SQLiteDatabase.CONFLICT_REPLACE, picValues)
                    values.put("pictureId", pictureId)
                } else {
                    values.putNull("pictureId")
                }

                db.insert("plant_new", SQLiteDatabase.CONFLICT_REPLACE, values)
            } while (plantCursor.moveToNext())
        }
        plantCursor.close()

        db.execSQL("DROP TABLE plant")
        db.execSQL("ALTER TABLE plant_new RENAME TO plant")

        // ---------------------------------------------------------
        // 4. diary 테이블 마이그레이션
        //    기존: pictureList TEXT (JSON)
        //    변경: pictureList 컬럼 제거, 사진은 picture + diary_picture 테이블로 이전
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `diary_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `plantId` INTEGER NOT NULL,
                `memo` TEXT NOT NULL,
                `createdDate` TEXT NOT NULL
            )
            """
        )

        val diaryCursor = db.query("SELECT * FROM diary")
        if (diaryCursor.moveToFirst()) {
            do {
                val diaryId = diaryCursor.getInt(diaryCursor.getColumnIndexOrThrow("id"))

                val diaryValues = ContentValues()
                diaryValues.put("id", diaryId)
                diaryValues.put("plantId", diaryCursor.getInt(diaryCursor.getColumnIndexOrThrow("plantId")))
                diaryValues.put("memo", diaryCursor.getString(diaryCursor.getColumnIndexOrThrow("memo")))
                diaryValues.put("createdDate", diaryCursor.getString(diaryCursor.getColumnIndexOrThrow("createdDate")))

                db.insert("diary_new", SQLiteDatabase.CONFLICT_REPLACE, diaryValues)

                // 기존 pictureList JSON에서 picture 테이블 + diary_picture 테이블로 이전
                val picListIndex = diaryCursor.getColumnIndexOrThrow("pictureList")
                if (!diaryCursor.isNull(picListIndex)) {
                    val jsonStr = diaryCursor.getString(picListIndex)
                    if (!jsonStr.isNullOrEmpty() && jsonStr != "null") {
                        try {
                            val jsonArray = JSONArray(jsonStr)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val path = obj.getString("path")
                                val fileName = obj.optString("fileName", File(path).name)
                                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                                val picValues = ContentValues()
                                picValues.put("path", path)
                                picValues.put("fileName", fileName)
                                picValues.put("createdAt", createdAt)

                                val pictureId = db.insert("picture", SQLiteDatabase.CONFLICT_REPLACE, picValues)

                                val crossRefValues = ContentValues()
                                crossRefValues.put("diaryId", diaryId)
                                crossRefValues.put("pictureId", pictureId)
                                db.insert("diary_picture", SQLiteDatabase.CONFLICT_IGNORE, crossRefValues)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } while (diaryCursor.moveToNext())
        }
        diaryCursor.close()

        db.execSQL("DROP TABLE diary")
        db.execSQL("ALTER TABLE diary_new RENAME TO diary")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_plantId` ON `diary` (`plantId`)")
    }
}
