package com.mskwak.common_ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.common_ui.IconPack

val IconPack.Gardening: ImageVector
    get() {
        if (_Gardening != null) {
            return _Gardening!!
        }
        _Gardening = ImageVector.Builder(
            name = "Gardening",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF9E4A14)),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(11.9f, 16.9f)
                lineTo(17.7f, 7.3f)
            }
            path(
                stroke = SolidColor(Color(0xFF557A1E)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(17.7f, 7.3f)
                lineTo(18.6f, 5.9f)
            }
            path(
                stroke = SolidColor(Color(0xFF557A1E)),
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 1.9f)
                lineTo(22.4f, 3.6f)
                lineTo(18.9f, 6.3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF7C8791))) {
                moveTo(9.5f, 14.3f)
                lineTo(15.7f, 17f)
                quadTo(15.2f, 19.5f, 13.8f, 20.8f)
                quadTo(11.2f, 21f, 8.9f, 19f)
                quadTo(8.9f, 16f, 9.5f, 14.3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF626C76))) {
                moveTo(10.9f, 15f)
                lineTo(14f, 16.4f)
                quadTo(13.6f, 18.2f, 12.6f, 18.8f)
                quadTo(11.3f, 18.6f, 10.6f, 17.7f)
                quadTo(10.6f, 16f, 10.9f, 15f)
                close()
            }
            path(fill = SolidColor(Color(0xFF8A3E0B))) {
                moveTo(3.3f, 22.8f)
                quadTo(3.6f, 20.9f, 4.9f, 20.7f)
                quadTo(5.8f, 19.6f, 7.2f, 19.6f)
                quadTo(8.4f, 18.9f, 9.9f, 19f)
                quadTo(11.6f, 18.8f, 13f, 19.3f)
                quadTo(14.6f, 19.4f, 15.4f, 20.4f)
                quadTo(16.9f, 21f, 17.2f, 22.8f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF8CC63F)),
                strokeLineWidth = 0.7f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6.2f, 19.9f)
                quadTo(5.9f, 17.5f, 5.4f, 15.4f)
                quadTo(4.9f, 13.4f, 4.7f, 11.7f)
                quadTo(4.6f, 10.5f, 5.3f, 10.1f)
                quadTo(6f, 9.9f, 5.9f, 10.6f)
                quadTo(5.7f, 11.2f, 5.1f, 10.9f)
            }
            path(fill = SolidColor(Color(0xFF6AA524))) {
                moveTo(5.2f, 13f)
                quadTo(6.5f, 11.3f, 8.4f, 12.2f)
                quadTo(7f, 13.9f, 5.2f, 13f)
                close()
                moveTo(4.9f, 15.1f)
                quadTo(3.3f, 13.8f, 1.3f, 15.3f)
                quadTo(3.1f, 16.7f, 4.9f, 15.1f)
                close()
                moveTo(5.7f, 17.2f)
                quadTo(7.1f, 15.6f, 8.6f, 16.9f)
                quadTo(7.1f, 18.3f, 5.7f, 17.2f)
                close()
            }
        }.build()

        return _Gardening!!
    }

@Suppress("ObjectPropertyName")
private var _Gardening: ImageVector? = null
