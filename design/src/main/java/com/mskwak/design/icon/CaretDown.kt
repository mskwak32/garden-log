package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.CaretDown: ImageVector
    get() {
        if (_CaretDown != null) {
            return _CaretDown!!
        }
        _CaretDown = ImageVector.Builder(
            name = "CaretDown",
            defaultWidth = 12.dp,
            defaultHeight = 12.dp,
            viewportWidth = 12f,
            viewportHeight = 12f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF51565B)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.91f, 9.286f)
                curveToRelative(-0.231f, 0f, -0.454f, -0.093f, -0.618f, -0.257f)
                lineTo(1.756f, 5.494f)
                curveToRelative(-0.341f, -0.342f, -0.341f, -0.896f, 0f, -1.238f)
                curveToRelative(0.342f, -0.341f, 0.896f, -0.341f, 1.238f, 0f)
                lineTo(5.91f, 7.173f)
                lineToRelative(2.916f, -2.917f)
                curveToRelative(0.342f, -0.341f, 0.896f, -0.341f, 1.238f, 0f)
                curveToRelative(0.341f, 0.342f, 0.341f, 0.896f, 0f, 1.238f)
                lineTo(6.529f, 9.029f)
                curveToRelative(-0.164f, 0.164f, -0.386f, 0.257f, -0.618f, 0.257f)
            }
        }.build()

        return _CaretDown!!
    }

@Suppress("ObjectPropertyName")
private var _CaretDown: ImageVector? = null
