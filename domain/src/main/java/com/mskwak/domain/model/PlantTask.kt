package com.mskwak.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class PlantTaskType {
    ONE_TIME,
    REPEAT
}

data class PlantTask(
    val id: Int = 0,
    val plantId: Int,
    val title: String,
    val content: String?,
    val type: PlantTaskType,
    // 1회성은 예정일, 반복성은 다음 예정일 계산의 기준일
    val date: LocalDate,
    // 반복성만 사용
    val periodDays: Int?,
    val alarm: Alarm,
    val createdAt: LocalDateTime,
    // 조회 시 완료 이력으로 채우는 값
    val latestDoneDate: LocalDate? = null,
    val isDoneToday: Boolean = false
)

// 할일 삭제 후에도 표시할 수 있도록 완료 시점의 제목을 함께 보관
data class PlantTaskLog(
    val plantId: Int,
    val taskId: Int?,
    val title: String,
    val date: LocalDate
)
