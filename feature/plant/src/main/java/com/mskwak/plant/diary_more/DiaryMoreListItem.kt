package com.mskwak.plant.diary_more

import androidx.compose.runtime.Immutable
import com.mskwak.plant.model.DiaryListItemUiModel
import java.time.YearMonth

@Immutable
sealed interface DiaryMoreListItem {
    data class MonthHeader(val yearMonth: YearMonth) : DiaryMoreListItem
    data class DiaryItem(val diary: DiaryListItemUiModel) : DiaryMoreListItem
}

fun buildDiaryListItems(diaries: List<DiaryListItemUiModel>): List<DiaryMoreListItem> {
    if (diaries.isEmpty()) return emptyList()

    val items = mutableListOf<DiaryMoreListItem>()
    var lastYearMonth: YearMonth? = null

    for (diary in diaries) {
        val yearMonth = YearMonth.from(diary.date)
        if (yearMonth != lastYearMonth) {
            items.add(DiaryMoreListItem.MonthHeader(yearMonth))
            lastYearMonth = yearMonth
        }
        items.add(DiaryMoreListItem.DiaryItem(diary))
    }

    return items
}
