package com.mskwak.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS plant_task (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId INTEGER NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                type TEXT NOT NULL,
                date TEXT NOT NULL,
                periodDays INTEGER,
                createdAt TEXT NOT NULL,
                alarm_time TEXT NOT NULL,
                alarm_isActive INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_task_plantId ON plant_task (plantId)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS plant_task_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId INTEGER NOT NULL,
                taskId INTEGER,
                title TEXT NOT NULL,
                date TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_task_log_plantId_date ON plant_task_log (plantId, date)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_plant_task_log_taskId_date ON plant_task_log (taskId, date)")
    }
}
