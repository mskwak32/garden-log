package com.mskwak.plant.util

import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import com.mskwak.plant.R

fun Exception.wateringErrorResource(): Int = when ((this as? WateringValidationException)?.reason) {
    WateringValidationReason.OUTSIDE_RANGE -> R.string.message_watering_outside_range
    WateringValidationReason.HARVESTED -> R.string.message_watering_harvested
    WateringValidationReason.FUTURE_CREATED_DATE -> R.string.message_created_future
    WateringValidationReason.CREATED_AFTER_WATERING_DATE -> R.string.message_created_after_watering
    null -> R.string.message_save_failed
}
