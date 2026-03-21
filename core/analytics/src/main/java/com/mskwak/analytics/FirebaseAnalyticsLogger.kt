package com.mskwak.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseAnalyticsLogger @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsLogger {

    override fun log(event: GardenEvent) {
        when (event) {
            is GardenEvent.ScreenView -> firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, event.screenName)
                }
            )

            is GardenEvent.AddPlant -> firebaseAnalytics.logEvent(
                "add_plant",
                Bundle().apply {
                    putLong("watering_interval", event.wateringInterval.toLong())
                    putString("alarm_enabled", event.alarmEnabled.toString())
                }
            )

            is GardenEvent.UpdatePlant -> firebaseAnalytics.logEvent("update_plant", null)

            is GardenEvent.AddDiary -> firebaseAnalytics.logEvent("add_diary", null)

            is GardenEvent.WateringClick -> firebaseAnalytics.logEvent(
                "watering_click",
                Bundle().apply {
                    putString("source", event.source.name.lowercase())
                }
            )

            is GardenEvent.WateringAlarmToggle -> firebaseAnalytics.logEvent(
                "watering_alarm_toggle",
                Bundle().apply {
                    putString("enabled", event.enabled.toString())
                }
            )

            is GardenEvent.Harvest -> firebaseAnalytics.logEvent("harvest", null)

            is GardenEvent.CancelHarvest -> firebaseAnalytics.logEvent("cancel_harvest", null)
        }
    }
}
