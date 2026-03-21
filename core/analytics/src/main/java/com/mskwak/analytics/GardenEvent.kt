package com.mskwak.analytics

sealed class GardenEvent {
    /** 화면 진입 이벤트 */
    data class ScreenView(val screenName: String) : GardenEvent()

    /** 식물 추가 */
    data class AddPlant(
        val wateringInterval: Int,
        val alarmEnabled: Boolean
    ) : GardenEvent()

    /** 식물 수정 */
    data object UpdatePlant : GardenEvent()

    /** 일기 추가 */
    data object AddDiary : GardenEvent()

    /** 물주기 버튼 클릭 */
    data class WateringClick(val source: WateringSource) : GardenEvent()

    /** 물주기 알림 on/off */
    data class WateringAlarmToggle(val enabled: Boolean) : GardenEvent()

    /** 수확 */
    data object Harvest : GardenEvent()

    /** 수확 취소 */
    data object CancelHarvest : GardenEvent()
}

enum class WateringSource { DETAIL, LIST }
