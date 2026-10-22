package com.mskwak.gardendailylog.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mskwak.domain.repository.PlantTaskAlarmRepository
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

internal class PlantTaskAlarmRepositoryImpl @Inject constructor(
    private val application: Application
) : PlantTaskAlarmRepository {
    private val alarmManager: AlarmManager =
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun setTaskAlarm(taskId: Int, nextAlarmDateTime: LocalDateTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Timber.e("Cannot schedule exact alarm: permission not granted")
            return
        }

        val intent = Intent(application, PlantTaskAlarmReceiver::class.java).apply {
            putExtra(PlantTaskAlarmReceiver.TASK_ID_KEY, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            taskId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(nextAlarmDateTime.getTimeMillis(), pendingIntent),
            pendingIntent
        )
        Timber.d("set task alarm: taskId= $taskId trigger= $nextAlarmDateTime")
    }

    override fun cancelTaskAlarm(taskId: Int) {
        //FLAG_NO_CREATE로 기존 생성된 pendingIntent 반환
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            taskId,
            Intent(application, PlantTaskAlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Timber.d("cancel task alarm: taskId=$taskId")
        }
    }

    private fun LocalDateTime.getTimeMillis(): Long {
        // 시스템의 기본 시간대를 기준으로 ZonedDateTime으로 변환한 후 밀리초로 변경
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}