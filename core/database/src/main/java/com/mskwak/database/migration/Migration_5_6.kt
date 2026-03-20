package com.mskwak.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE plant ADD COLUMN isHarvested INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE plant ADD COLUMN harvestMemo TEXT")
    }
}
