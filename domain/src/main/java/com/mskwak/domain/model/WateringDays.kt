package com.mskwak.domain.model

/**
 * @param days 물준 날짜로 부터의 차이. 이미 지났건 남았건 날짜의 절대값 차이
 * @param isOverDue 물주기가 지난 경우 true
 */
data class WateringDays(
    val days: Int,
    val isOverDue: Boolean
)
