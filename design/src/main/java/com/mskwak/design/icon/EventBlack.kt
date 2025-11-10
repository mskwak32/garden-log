package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.EventBlack: ImageVector
    get() {
        if (_EventBlack != null) {
            return _EventBlack!!
        }
        _EventBlack = ImageVector.Builder(
            name = "EventBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 4f)
                horizontalLineToRelative(-1f)
                lineTo(18f, 2f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(2f)
                lineTo(8f, 4f)
                lineTo(8f, 2f)
                lineTo(6f, 2f)
                verticalLineToRelative(2f)
                lineTo(5f, 4f)
                curveToRelative(-1.11f, 0f, -1.99f, 0.9f, -1.99f, 2f)
                lineTo(3f, 20f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                lineTo(21f, 6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(19f, 20f)
                lineTo(5f, 20f)
                lineTo(5f, 10f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(10f)
                close()
                moveTo(19f, 8f)
                lineTo(5f, 8f)
                lineTo(5f, 6f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(2f)
                close()
                moveTo(12f, 13f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(-5f)
                close()
            }
        }.build()

        return _EventBlack!!
    }

@Suppress("ObjectPropertyName")
private var _EventBlack: ImageVector? = null
