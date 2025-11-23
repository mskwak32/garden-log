package com.mskwak.data.mapper

import com.mskwak.database.entity.AlarmEntity
import com.mskwak.database.entity.PictureEntity
import com.mskwak.database.entity.PlantEntity
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Picture
import com.mskwak.domain.model.Plant

internal fun Plant.toPlantEntity(): PlantEntity {
    return PlantEntity(
        id = id,
        name = name,
        createdDate = createdDate,
        waterPeriod = waterPeriod,
        lastWateringDate = lastWateringDate,
        wateringAlarm = wateringAlarm.toAlarmEntity(),
        picture = picture?.toPictureEntity(),
        memo = memo
    )
}

internal fun PlantEntity.toPlant(): Plant {
    return Plant(
        id = id,
        name = name,
        createdDate = createdDate,
        waterPeriod = waterPeriod,
        lastWateringDate = lastWateringDate,
        wateringAlarm = wateringAlarm.toAlarm(),
        picture = picture?.toPicture(),
        memo = memo
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