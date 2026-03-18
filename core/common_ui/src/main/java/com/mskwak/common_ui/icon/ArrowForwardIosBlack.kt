package com.mskwak.common_ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack

val IconPack.ArrowForwardIosBlack: ImageVector
    get() {
        if (_ArrowForwardIosBlack != null) {
            return _ArrowForwardIosBlack!!
        }
        _ArrowForwardIosBlack = ImageVector.Builder(
            name = "ArrowForwardIosBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.23f, 20.23f)
                lineToRelative(1.77f, 1.77f)
                lineToRelative(10f, -10f)
                lineToRelative(-10f, -10f)
                lineToRelative(-1.77f, 1.77f)
                lineToRelative(8.23f, 8.23f)
                close()
            }
        }.build()

        return _ArrowForwardIosBlack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowForwardIosBlack: ImageVector? = null
