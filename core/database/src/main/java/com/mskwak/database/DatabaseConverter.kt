package com.mskwak.database

import androidx.room.TypeConverter
import com.mskwak.database.entity.PictureEntity
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal class DatabaseConverter {
    @TypeConverter
    fun dateToString(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun stringToDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun timeToString(time: LocalTime): String {
        return time.toString()
    }

    @TypeConverter
    fun stringToTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString)
    }

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }

    @TypeConverter
    fun stringToDateTime(dateTimeString: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeString)
    }

    @TypeConverter
    fun pictureListToJson(pictures: List<PictureEntity>): String {
        return Json.encodeToString(pictures)
    }

    @TypeConverter
    fun jsonToPictureList(json: String): List<PictureEntity> {
        return Json.decodeFromString(json)
    }
}