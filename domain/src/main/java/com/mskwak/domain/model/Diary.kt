package com.mskwak.domain.model

import java.time.LocalDate

data class Diary(
    val id: Int,
    val plantId: Int,
    val memo: String,
    val pictureList: List<Picture>?,
    val createdDate: LocalDate
)

enum class DiaryListSortOder {
    CREATED_LATEST,
    CREATED_EARLIEST
}