package com.mskwak.gardendailylog.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mskwak.domain.repository.WateringAlarmRepository
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

internal class WateringAlarmRepositoryImpl @Inject constructor(
    private val application: Application
) : WateringAlarmRepository {
    private val alarmManager: AlarmManager =
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun setWateringAlarm(plantId: Int, nextAlarmDateTime: LocalDateTime) {
        val intent = Intent(application, WateringAlarmReceiver::class.java).apply {
            putExtra(WateringAlarmReceiver.PLANT_ID_KEY, plantId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            plantId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(nextAlarmDateTime.getTimeMillis(), pendingIntent),
            pendingIntent
        )
        Timber.d("set watering alarm: plantId= $plantId trigger= $nextAlarmDateTime")
    }

    override fun cancelWateringAlarm(plantId: Int) {
        //FLAG_NO_CREATE로 기존 생성된 pendingIntent 반환
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            plantId,
            Intent(application, WateringAlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Timber.d("cancel watering alarm: plantId=$plantId")
        }
    }

    private fun LocalDateTime.getTimeMillis(): Long {
        // 시스템의 기본 시간대를 기준으로 ZonedDateTime으로 변환한 후 밀리초로 변경
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}