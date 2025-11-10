package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.DescriptionBlack: ImageVector
    get() {
        if (_DescriptionBlack != null) {
            return _DescriptionBlack!!
        }
        _DescriptionBlack = ImageVector.Builder(
            name = "DescriptionBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 16f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(2f)
                lineTo(8f, 18f)
                close()
                moveTo(8f, 12f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(2f)
                lineTo(8f, 14f)
                close()
                moveTo(14f, 2f)
                lineTo(6f, 2f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(16f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 1.99f, 2f)
                lineTo(18f, 22f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                lineTo(20f, 8f)
                lineToRelative(-6f, -6f)
                close()
                moveTo(18f, 20f)
                lineTo(6f, 20f)
                lineTo(6f, 4f)
                horizontalLineToRelative(7f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(11f)
                close()
            }
        }.build()

        return _DescriptionBlack!!
    }

@Suppress("ObjectPropertyName")
private var _DescriptionBlack: ImageVector? = null
