package com.mskwak.gardendailylog.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.task.SetPlantTaskAlarmUseCase
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 부팅 후 또는 앱 업데이트 후 리시버
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var plantRepository: PlantRepository

    @Inject
    lateinit var setWateringAlarmUseCase: SetWateringAlarmUseCase

    @Inject
    lateinit var plantTaskRepository: PlantTaskRepository

    @Inject
    lateinit var setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action?.equals(Intent.ACTION_BOOT_COMPLETED) == true ||
            intent?.action?.equals(Intent.ACTION_MY_PACKAGE_REPLACED) == true
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                resetWateringAlarm()
                resetTaskAlarm()
            }
        }
    }

    /**
     * 물주기 알람 재등록
     */
    private suspend fun resetWateringAlarm() {
        plantRepository.getPlantIdsWithAlarmActivation().forEach { (plantId, isActive) ->
            if (isActive) {
                setWateringAlarmUseCase(plantId, true)
            }
        }
    }

    /**
     * 할일 알람 재등록
     */
    private suspend fun resetTaskAlarm() {
        plantTaskRepository.getActiveAlarmTaskIds().forEach {
            setPlantTaskAlarmUseCase(it, true)
        }
    }
}
