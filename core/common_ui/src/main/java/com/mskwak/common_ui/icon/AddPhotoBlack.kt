package com.mskwak.common_ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack

val IconPack.AddPhotoBlack: ImageVector
    get() {
        if (_AddPhotoBlack != null) {
            return _AddPhotoBlack!!
        }
        _AddPhotoBlack = ImageVector.Builder(
            name = "AddPhotoBlack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 6f)
                horizontalLineToRelative(-3.17f)
                lineTo(16f, 4f)
                horizontalLineToRelative(-6f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(5.12f)
                lineToRelative(1.83f, 2f)
                lineTo(21f, 8f)
                verticalLineToRelative(12f)
                lineTo(5f, 20f)
                verticalLineToRelative(-9f)
                lineTo(3f, 11f)
                verticalLineToRelative(9f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                lineTo(23f, 8f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(8f, 14f)
                curveToRelative(0f, 2.76f, 2.24f, 5f, 5f, 5f)
                reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
                reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
                reflectiveCurveToRelative(-5f, 2.24f, -5f, 5f)
                close()
                moveTo(13f, 11f)
                curveToRelative(1.65f, 0f, 3f, 1.35f, 3f, 3f)
                reflectiveCurveToRelative(-1.35f, 3f, -3f, 3f)
                reflectiveCurveToRelative(-3f, -1.35f, -3f, -3f)
                reflectiveCurveToRelative(1.35f, -3f, 3f, -3f)
                close()
                moveTo(5f, 6f)
                horizontalLineToRelative(3f)
                lineTo(8f, 4f)
                lineTo(5f, 4f)
                lineTo(5f, 1f)
                lineTo(3f, 1f)
                verticalLineToRelative(3f)
                lineTo(0f, 4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2f)
                close()
            }
        }.build()

        return _AddPhotoBlack!!
    }

@Suppress("ObjectPropertyName")
private var _AddPhotoBlack: ImageVector? = null
