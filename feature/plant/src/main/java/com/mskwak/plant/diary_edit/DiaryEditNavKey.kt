package com.mskwak.plant.diary_edit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DiaryEditNavKey(val plantId: Int, val diaryId: Int? = null) : NavKey
