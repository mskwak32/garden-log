package com.mskwak.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS watering_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId INTEGER NOT NULL,
                date TEXT NOT NULL
            )
            """.trimIndent()
        )
        // Room이 기대하는 명명된 유니크 인덱스 생성
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_watering_log_plantId_date
            ON watering_log (plantId, date)
            """.trimIndent()
        )
        // 기존 lastWateringDate를 각 식물의 초기 물주기 이력으로 삽입
        db.execSQL(
            """
            INSERT OR IGNORE INTO watering_log (plantId, date)
            SELECT id, lastWateringDate FROM plant
            """.trimIndent()
        )
    }
}
