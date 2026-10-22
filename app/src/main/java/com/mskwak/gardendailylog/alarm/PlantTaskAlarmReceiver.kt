package com.mskwak.gardendailylog.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.plant.GetPlantNameUseCase
import com.mskwak.domain.usecase.task.SetPlantTaskAlarmUseCase
import com.mskwak.gardendailylog.MainActivity
import com.mskwak.gardendailylog.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlantTaskAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var plantTaskRepository: PlantTaskRepository

    @Inject
    lateinit var getPlantNameUseCase: GetPlantNameUseCase

    @Inject
    lateinit var setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(TASK_ID_KEY, DEFAULT_TASK_ID)

        createNotificationChannel(context)
        CoroutineScope(Dispatchers.Default).takeIf { taskId != DEFAULT_TASK_ID }?.launch {
            // 알람 이후 삭제된 할일은 알림 없이 종료
            val task = plantTaskRepository.getTask(taskId) ?: return@launch
            val plantName = getPlantNameUseCase.getName(task.plantId)

            showNotification(context, plantName, task.title, taskId)
            setPlantTaskAlarmUseCase(taskId, true)
        }
    }

    private fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.noti_task_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannel(channel)
    }

    private fun showNotification(
        context: Context,
        plantName: String?,
        taskTitle: String,
        taskId: Int
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_leaf)
                .setContentTitle(context.getString(R.string.noti_task_title, plantName.orEmpty()))
                .setContentText(taskTitle)
                .setContentIntent(getPendingIntent(context))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setGroup(TASK_GROUP_KEY)
                .build()

            with(NotificationManagerCompat.from(context)) {
                // 물주기 알림 ID(plantId)와 겹치지 않도록 tag 사용
                notify(NOTIFICATION_TAG, taskId, notification)
                notify(SUMMARY_NOTIFICATION_CODE, getSummaryNotification(context))
            }
        }
    }

    // Group 알림
    private fun getSummaryNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_leaf)
            .setAutoCancel(true)
            .setGroup(TASK_GROUP_KEY)
            .setOnlyAlertOnce(true)
            .setGroupSummary(true)
            .build()
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        internal const val TASK_ID_KEY = "taskId"
        private const val CHANNEL_ID = "plantTaskNotification"
        private const val DEFAULT_TASK_ID = -1
        private const val NOTIFICATION_TAG = "plantTask"
        private const val SUMMARY_NOTIFICATION_CODE = 2000
        private const val TASK_GROUP_KEY = "plantTaskNotificationGroup"
    }
}
