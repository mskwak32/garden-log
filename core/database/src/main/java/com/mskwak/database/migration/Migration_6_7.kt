package com.mskwak.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// isHarvested(Boolean) → harvestDate(TEXT, nullable)로 교체
// SQLite는 컬럼 삭제를 지원하지 않으므로 테이블을 재생성
internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE plant_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdDate TEXT NOT NULL,
                waterPeriod INTEGER NOT NULL,
                lastWateringDate TEXT NOT NULL,
                pictureId INTEGER,
                memo TEXT,
                harvestDate TEXT,
                harvestMemo TEXT,
                watering_alarm_time TEXT NOT NULL,
                watering_alarm_isActive INTEGER NOT NULL
            )
            """.trimIndent()
        )
        // 기존 isHarvested=1 행은 harvestDate를 알 수 없으므로 NULL로 마이그레이션
        db.execSQL(
            """
            INSERT INTO plant_new (id, name, createdDate, waterPeriod, lastWateringDate,
                watering_alarm_time, watering_alarm_isActive, pictureId, memo, harvestDate, harvestMemo)
            SELECT id, name, createdDate, waterPeriod, lastWateringDate,
                watering_alarm_time, watering_alarm_isActive, pictureId, memo, NULL, harvestMemo
            FROM plant
            """.trimIndent()
        )
        db.execSQL("DROP TABLE plant")
        db.execSQL("ALTER TABLE plant_new RENAME TO plant")
    }
}