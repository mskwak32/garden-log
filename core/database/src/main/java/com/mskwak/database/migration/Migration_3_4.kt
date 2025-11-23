package com.mskwak.database.migration

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ---------------------------------------------------------
        // 1. Plant 테이블 마이그레이션
        // ---------------------------------------------------------

        // 1-1. 새 스키마를 가진 임시 테이블 생성
        // 변경점: memo, picture 관련 컬럼들에서 'NOT NULL' 제약조건 제거
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
                `picture_path` TEXT,
                `picture_fileName` TEXT,
                `picture_createdAt` INTEGER,
                `memo` TEXT
            )
        """
        )

        // 1-2. 기존 데이터 조회
        var cursor = db.query("SELECT * FROM plant")

        // 1-3. 데이터 변환 및 복사
        if (cursor.moveToFirst()) {
            do {
                val values = ContentValues()

                // [그대로 복사하는 컬럼들]
                values.put("id", cursor.getInt(cursor.getColumnIndexOrThrow("id")))
                values.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                values.put(
                    "createdDate",
                    cursor.getString(cursor.getColumnIndexOrThrow("createdDate"))
                )
                values.put(
                    "waterPeriod",
                    cursor.getInt(cursor.getColumnIndexOrThrow("waterPeriod"))
                )
                values.put(
                    "lastWateringDate",
                    cursor.getString(cursor.getColumnIndexOrThrow("lastWateringDate"))
                )

                // [Memo] String? (Nullable) -> 그대로 복사 (Null이면 DB에 Null로 들어감)
                val memoIndex = cursor.getColumnIndexOrThrow("memo")
                if (cursor.isNull(memoIndex)) {
                    values.putNull("memo")
                } else {
                    values.put("memo", cursor.getString(memoIndex))
                }

                // [Alarm] Embedded 변환 (Not Null)
                // 기존 컬럼: time, onOff -> 새 컬럼: watering_alarm_time, watering_alarm_isActive
                values.put(
                    "watering_alarm_time",
                    cursor.getString(cursor.getColumnIndexOrThrow("time"))
                )
                values.put(
                    "watering_alarm_isActive",
                    cursor.getInt(cursor.getColumnIndexOrThrow("onOff"))
                )

                // [Picture] Uri? -> Embedded PictureEntity? 변환
                val uriIndex = cursor.getColumnIndexOrThrow("pictureUri")
                val oldUriString = cursor.getString(uriIndex)

                if (oldUriString != null) {
                    // 기존에 사진이 있었으면 새 구조로 변환하여 저장
                    val file = File(oldUriString)
                    values.put("picture_path", oldUriString)
                    values.put("picture_fileName", file.name ?: "unknown")
                    values.put("picture_createdAt", System.currentTimeMillis())
                } else {
                    // 기존에 사진이 없었으면(Null), 새 컬럼들도 모두 Null 저장
                    values.putNull("picture_path")
                    values.putNull("picture_fileName")
                    values.putNull("picture_createdAt")
                }

                db.insert("plant_new", SQLiteDatabase.CONFLICT_REPLACE, values)

            } while (cursor.moveToNext())
        }
        cursor.close()

        // 1-4. 테이블 교체
        db.execSQL("DROP TABLE plant")
        db.execSQL("ALTER TABLE plant_new RENAME TO plant")


        // ---------------------------------------------------------
        // 2. Diary 테이블 마이그레이션
        // ---------------------------------------------------------

        // 2-1. 새 스키마를 가진 임시 테이블 생성
        // 변경점: pictureList 컬럼에서 'NOT NULL' 제거
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `diary_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `plantId` INTEGER NOT NULL,
                `memo` TEXT NOT NULL,
                `pictureList` TEXT,
                `createdDate` TEXT NOT NULL
            )
        """
        )

        // 2-2. 기존 데이터 조회
        cursor = db.query("SELECT * FROM diary")

        // 2-3. 데이터 변환 및 복사
        if (cursor.moveToFirst()) {
            do {
                val values = ContentValues()

                values.put("id", cursor.getInt(cursor.getColumnIndexOrThrow("id")))
                values.put("plantId", cursor.getInt(cursor.getColumnIndexOrThrow("plantId")))
                values.put("memo", cursor.getString(cursor.getColumnIndexOrThrow("memo")))
                values.put(
                    "createdDate",
                    cursor.getString(cursor.getColumnIndexOrThrow("createdDate"))
                )

                // [PictureList] List<Uri>? -> List<PictureEntity>? 변환
                val picListIndex = cursor.getColumnIndexOrThrow("pictureList")
                val oldJsonStr = cursor.getString(picListIndex)

                // 기존 데이터가 Null이거나 빈 문자열이면 Null로 저장
                if (oldJsonStr.isNullOrEmpty() || oldJsonStr == "null") {
                    values.putNull("pictureList")
                } else {
                    val newJsonArray = JSONArray()
                    try {
                        // 기존: ["/path/a", "/path/b"]
                        val oldArray = JSONArray(oldJsonStr)

                        for (i in 0 until oldArray.length()) {
                            val path = oldArray.getString(i)
                            val file = File(path)

                            // 새 구조: {"path": "...", "fileName": "...", "createdAt": ...}
                            val newObj = JSONObject()
                            newObj.put("path", path)
                            newObj.put("fileName", file.name ?: "unknown")
                            newObj.put("createdAt", System.currentTimeMillis())

                            newJsonArray.put(newObj)
                        }
                        values.put("pictureList", newJsonArray.toString())
                    } catch (e: Exception) {
                        // 파싱 실패 시 안전하게 Null 처리 혹은 빈 배열 "[]" 처리
                        // 여기서는 Nullable이므로 Null 처리
                        values.putNull("pictureList")
                        e.printStackTrace()
                    }
                }

                db.insert("diary_new", SQLiteDatabase.CONFLICT_REPLACE, values)

            } while (cursor.moveToNext())
        }
        cursor.close()

        // 2-4. 테이블 교체 및 인덱스 복구
        db.execSQL("DROP TABLE diary")
        db.execSQL("ALTER TABLE diary_new RENAME TO diary")

        // DiaryEntity의 indices 복구
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diary_plantId` ON `diary` (`plantId`)")
    }
}