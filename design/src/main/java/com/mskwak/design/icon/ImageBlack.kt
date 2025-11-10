package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.ImageBlack: ImageVector
    get() {
        if (_ImageBlack != null) {
            return _ImageBlack!!
        }
        _ImageBlack = ImageVector.Builder(
            name = "ImageBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 5f)
                verticalLineToRelative(14f)
                lineTo(5f, 19f)
                lineTo(5f, 5f)
                horizontalLineToRelative(14f)
                moveToRelative(0f, -2f)
                lineTo(5f, 3f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(14f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                lineTo(21f, 5f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(14.14f, 11.86f)
                lineToRelative(-3f, 3.87f)
                lineTo(9f, 13.14f)
                lineTo(6f, 17f)
                horizontalLineToRelative(12f)
                lineToRelative(-3.86f, -5.14f)
                close()
            }
        }.build()

        return _ImageBlack!!
    }

@Suppress("ObjectPropertyName")
private var _ImageBlack: ImageVector? = null
