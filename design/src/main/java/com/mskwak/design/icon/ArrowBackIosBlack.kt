package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.ArrowBackIosBlack: ImageVector
    get() {
        if (_ArrowBackIosBlack != null) {
            return _ArrowBackIosBlack!!
        }
        _ArrowBackIosBlack = ImageVector.Builder(
            name = "ArrowBackIosBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.51f, 3.87f)
                lineTo(15.73f, 2.1f)
                lineTo(5.84f, 12f)
                lineToRelative(9.9f, 9.9f)
                lineToRelative(1.77f, -1.77f)
                lineTo(9.38f, 12f)
                lineToRelative(8.13f, -8.13f)
                close()
            }
        }.build()

        return _ArrowBackIosBlack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowBackIosBlack: ImageVector? = null
