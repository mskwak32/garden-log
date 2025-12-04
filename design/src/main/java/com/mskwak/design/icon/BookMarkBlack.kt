package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.BookMarkBlack: ImageVector
    get() {
        if (_BookMarkBlack != null) {
            return _BookMarkBlack!!
        }
        _BookMarkBlack = ImageVector.Builder(
            name = "BookMarkBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 2f)
                lineTo(6f, 2f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(16f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(12f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                lineTo(20f, 4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(9f, 4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(5f)
                lineToRelative(-1f, -0.75f)
                lineTo(9f, 9f)
                lineTo(9f, 4f)
                close()
                moveTo(18f, 20f)
                lineTo(6f, 20f)
                lineTo(6f, 4f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(9f)
                lineToRelative(3f, -2.25f)
                lineTo(13f, 13f)
                lineTo(13f, 4f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(16f)
                close()
            }
        }.build()

        return _BookMarkBlack!!
    }

@Suppress("ObjectPropertyName")
private var _BookMarkBlack: ImageVector? = null
