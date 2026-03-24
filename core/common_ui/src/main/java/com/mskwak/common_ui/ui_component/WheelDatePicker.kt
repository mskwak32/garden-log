package com.mskwak.common_ui.ui_component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.R
import com.mskwak.common_ui.theme.GardenLogTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WheelDatePicker(
    initialDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val years = remember(minDate, maxDate) {
        (minDate.year..maxDate.year).toList()
    }

    var selectedYear by remember {
        mutableIntStateOf(
            initialDate.year.coerceIn(
                minDate.year,
                maxDate.year
            )
        )
    }
    var selectedMonth by remember { mutableIntStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableIntStateOf(initialDate.dayOfMonth) }

    val validMonths = remember(selectedYear, minDate, maxDate) {
        val minM = if (selectedYear == minDate.year) minDate.monthValue else 1
        val maxM = if (selectedYear == maxDate.year) maxDate.monthValue else 12
        (minM..maxM).toList()
    }

    val validDays = remember(selectedYear, selectedMonth, minDate, maxDate) {
        val daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
        val minD =
            if (selectedYear == minDate.year && selectedMonth == minDate.monthValue) minDate.dayOfMonth else 1
        val maxD = if (selectedYear == maxDate.year && selectedMonth == maxDate.monthValue) minOf(
            maxDate.dayOfMonth,
            daysInMonth
        ) else daysInMonth
        (minD..maxD).toList()
    }

    fun notifyDate() {
        val safeMonth = selectedMonth.coerceIn(validMonths.first(), validMonths.last())
        val safeDay = selectedDay.coerceIn(validDays.first(), validDays.last())
        onDateSelected(LocalDate.of(selectedYear, safeMonth, safeDay))
    }

    Row(modifier = modifier) {
        WheelColumn(
            items = years.map { stringResource(R.string.year_format, it) },
            selectedIndex = (years.indexOf(selectedYear)).coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                selectedYear = years[index]
                selectedMonth = selectedMonth.coerceIn(
                    validMonths.firstOrNull() ?: 1,
                    validMonths.lastOrNull() ?: 12
                )
                selectedDay =
                    selectedDay.coerceIn(validDays.firstOrNull() ?: 1, validDays.lastOrNull() ?: 28)
                notifyDate()
            },
            modifier = Modifier.weight(3f)
        )
        WheelColumn(
            items = validMonths.map { stringResource(R.string.month_format, it) },
            selectedIndex = (validMonths.indexOf(selectedMonth)).coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                selectedMonth = validMonths[index]
                selectedDay =
                    selectedDay.coerceIn(validDays.firstOrNull() ?: 1, validDays.lastOrNull() ?: 28)
                notifyDate()
            },
            modifier = Modifier.weight(2f)
        )
        WheelColumn(
            items = validDays.map { stringResource(R.string.day_format, it) },
            selectedIndex = (validDays.indexOf(selectedDay)).coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                selectedDay = validDays[index]
                notifyDate()
            },
            modifier = Modifier.weight(2f)
        )
    }
}

private val ITEM_HEIGHT = 44.dp
private const val VISIBLE_COUNT = 3
private const val PADDING_COUNT = VISIBLE_COUNT / 2 // = 1

@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 리스트 구성: [padding * 2] + items + [padding * 2]
    // firstVisibleItemIndex == selectedIndex 일 때 해당 아이템이 중앙에 위치
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // 스크롤이 멈췄을 때 선택된 아이템 계산
    // firstVisible == selectedIndex 일 때 해당 아이템이 중앙에 위치하므로 그대로 사용
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val realIndex = listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
            if (realIndex != selectedIndex) {
                onSelectedIndexChange(realIndex)
            }
        }
    }

    // 외부에서 selectedIndex가 변경될 때 스크롤 동기화
    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex && !listState.isScrollInProgress) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(modifier = modifier.height(ITEM_HEIGHT * VISIBLE_COUNT)) {
        // 선택 영역 배경
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(ITEM_HEIGHT)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 패딩 아이템
            items(PADDING_COUNT) {
                Box(modifier = Modifier
                    .height(ITEM_HEIGHT)
                    .fillMaxWidth())
            }
            // 실제 아이템
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = if (isSelected) MaterialTheme.typography.bodyLarge
                        else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
            // 하단 패딩 아이템
            items(PADDING_COUNT) {
                Box(modifier = Modifier
                    .height(ITEM_HEIGHT)
                    .fillMaxWidth())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WheelDatePickerPreview() {
    GardenLogTheme {
        WheelDatePicker(
            initialDate = LocalDate.of(2024, 6, 15),
            minDate = LocalDate.of(2023, 1, 1),
            maxDate = LocalDate.of(2025, 12, 31),
            onDateSelected = {}
        )
    }
}
