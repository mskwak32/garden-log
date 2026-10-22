package com.mskwak.data.mapper

import com.mskwak.database.entity.AlarmEntity
import com.mskwak.database.entity.DiaryEntity
import com.mskwak.database.entity.PictureEntity
import com.mskwak.database.entity.PlantEntity
import com.mskwak.database.entity.PlantTaskEntity
import com.mskwak.database.entity.PlantTaskLogEntity
import com.mskwak.database.entity.PlantTaskWithDone
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Diary
import com.mskwak.domain.model.Picture
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskLog
import com.mskwak.domain.model.PlantTaskType

internal fun Plant.toPlantEntity(pictureId: Int?): PlantEntity {
    return PlantEntity(
        id = id,
        name = name,
        createdDate = createdDate,
        waterPeriod = waterPeriod,
        lastWateringDate = lastWateringDate,
        wateringAlarm = wateringAlarm.toAlarmEntity(),
        pictureId = pictureId,
        memo = memo,
        harvestDate = harvestDate,
        harvestMemo = harvestMemo
    )
}

internal fun PlantEntity.toPlant(picture: PictureEntity?): Plant {
    return Plant(
        id = id,
        name = name,
        createdDate = createdDate,
        waterPeriod = waterPeriod,
        lastWateringDate = lastWateringDate,
        wateringAlarm = wateringAlarm.toAlarm(),
        picture = picture?.toPicture(),
        memo = memo,
        harvestDate = harvestDate,
        harvestMemo = harvestMemo
    )
}

internal fun Alarm.toAlarmEntity(): AlarmEntity {
    return AlarmEntity(
        time = time,
        isActive = isActive
    )
}

internal fun AlarmEntity.toAlarm(): Alarm {
    return Alarm(
        time = time,
        isActive = isActive
    )
}

internal fun Picture.toPictureEntity(): PictureEntity {
    return PictureEntity(
        path = path,
        fileName = fileName,
        createdAt = createdAt
    )
}

internal fun PictureEntity.toPicture(): Picture {
    return Picture(
        path = path,
        fileName = fileName,
        createdAt = createdAt
    )
}

internal fun Diary.toDiaryEntity(): DiaryEntity {
    return DiaryEntity(
        id = id,
        plantId = plantId,
        memo = memo,
        createdDate = createdDate
    )
}

internal fun DiaryEntity.toDiary(pictures: List<PictureEntity>): Diary {
    return Diary(
        id = id,
        plantId = plantId,
        memo = memo,
        pictureList = pictures.map { it.toPicture() }.ifEmpty { null },
        createdDate = createdDate
    )
}

internal fun PlantTask.toPlantTaskEntity(): PlantTaskEntity {
    return PlantTaskEntity(
        id = id,
        plantId = plantId,
        title = title,
        content = content,
        type = type.name,
        date = date,
        periodDays = periodDays,
        alarm = alarm.toAlarmEntity(),
        createdAt = createdAt
    )
}

internal fun PlantTaskEntity.toPlantTask(
    latestDoneDate: java.time.LocalDate? = null,
    isDoneToday: Boolean = false
): PlantTask {
    return PlantTask(
        id = id,
        plantId = plantId,
        title = title,
        content = content,
        type = PlantTaskType.valueOf(type),
        date = date,
        periodDays = periodDays,
        alarm = alarm.toAlarm(),
        createdAt = createdAt,
        latestDoneDate = latestDoneDate,
        isDoneToday = isDoneToday
    )
}

internal fun PlantTaskWithDone.toPlantTask(): PlantTask =
    task.toPlantTask(latestDoneDate, isDoneToday)

internal fun PlantTaskLogEntity.toPlantTaskLog(): PlantTaskLog {
    return PlantTaskLog(
        plantId = plantId,
        taskId = taskId,
        title = title,
        date = date
    )
}
