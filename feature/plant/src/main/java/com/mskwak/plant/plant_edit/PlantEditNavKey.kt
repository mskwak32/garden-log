package com.mskwak.plant.plant_edit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class PlantEditNavKey(val plantId: Int? = null) : NavKey
