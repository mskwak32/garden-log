package com.mskwak.plant

import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringDays
import com.mskwak.plant.model.WateringStatus
import com.mskwak.plant.model.toPlantListItemUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class WateringStatusTest {
    @Test
    fun fallbackDateTodayDoesNotShowCompletionWithoutActualRecord() {
        val today = LocalDate.now()
        val plant = Plant(
            id = 1, name = "식물", createdDate = today, waterPeriod = 3,
            lastWateringDate = today, wateringAlarm = Alarm(LocalTime.NOON, false),
            picture = null, memo = null, latestWateringDate = null, isWateredToday = false
        )
        val uiModel = plant.toPlantListItemUiModel { WateringDays(3, false, true) }
        assertEquals(WateringStatus.UPCOMING, uiModel.status)
    }

    @Test
    fun actualTodayRecordShowsCompletionEvenWhenLatestLegacyRecordIsFuture() {
        val today = LocalDate.now()
        val future = today.plusDays(1)
        val plant = Plant(
            id = 1, name = "식물", createdDate = today.minusDays(10), waterPeriod = 3,
            lastWateringDate = future, wateringAlarm = Alarm(LocalTime.NOON, false),
            picture = null, memo = null, latestWateringDate = future, isWateredToday = true
        )
        val uiModel = plant.toPlantListItemUiModel { WateringDays(4, false, true) }
        assertEquals(WateringStatus.TODAY_DONE, uiModel.status)
    }
}
