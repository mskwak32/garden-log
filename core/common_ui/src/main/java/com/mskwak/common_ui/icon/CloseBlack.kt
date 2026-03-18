package com.mskwak.common_ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack

val IconPack.CloseBlack: ImageVector
    get() {
        if (_CloseBlack != null) {
            return _CloseBlack!!
        }
        _CloseBlack = ImageVector.Builder(
            name = "CloseBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 6.41f)
                lineTo(17.59f, 5f)
                lineTo(12f, 10.59f)
                lineTo(6.41f, 5f)
                lineTo(5f, 6.41f)
                lineTo(10.59f, 12f)
                lineTo(5f, 17.59f)
                lineTo(6.41f, 19f)
                lineTo(12f, 13.41f)
                lineTo(17.59f, 19f)
                lineTo(19f, 17.59f)
                lineTo(13.41f, 12f)
                lineTo(19f, 6.41f)
                close()
            }
        }.build()

        return _CloseBlack!!
    }

@Suppress("ObjectPropertyName")
private var _CloseBlack: ImageVector? = null
