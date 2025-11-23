package com.mskwak.domain.model

import java.time.LocalTime

data class Alarm(
    val time: LocalTime,
    val isActivate: Boolean
)
