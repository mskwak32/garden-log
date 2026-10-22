package com.mskwak.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.mskwak.database.migration.MIGRATION_8_9
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GardenDatabase::class.java
    )

    @Test
    fun migrate8To9CreatesTaskTablesMatchingSchema() {
        helper.createDatabase("migration-test", 8).close()
        helper.runMigrationsAndValidate("migration-test", 9, true, MIGRATION_8_9).close()
    }
}
