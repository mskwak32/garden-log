package com.mskwak.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mskwak.design.IconPack

val IconPack.WaterDropWithBackground: ImageVector
    get() {
        if (_WaterDropWithBackground != null) {
            return _WaterDropWithBackground!!
        }
        _WaterDropWithBackground = ImageVector.Builder(
            name = "WaterDropWithBackground",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // 1. 배경: 하늘색 동그라미 (24x24 꽉 채움)
            path(fill = SolidColor(Color(0xFF81CFE2))) {
                // (12, 12)를 중심으로 반지름 12인 원 그리기
                moveTo(12f, 0f)
                curveTo(18.627f, 0f, 24f, 5.373f, 24f, 12f)
                curveTo(24f, 18.627f, 18.627f, 24f, 12f, 24f)
                curveTo(5.373f, 24f, 0f, 18.627f, 0f, 12f)
                curveTo(0f, 5.373f, 5.373f, 0f, 12f, 0f)
                close()
            }

            // 2. 전경: 흰색 물방울 (2dp 패딩 적용)
            group(
                scaleX = 20f / 24f, // 0.8333...
                scaleY = 20f / 24f,
                translationX = 2f,
                translationY = 2f
            ) {
                path(fill = SolidColor(Color.White)) {
                    // 기존 WaterDropWhite의 path 데이터 그대로 사용
                    moveTo(12f, 2f)
                    curveToRelative(-5.33f, 4.55f, -8f, 8.48f, -8f, 11.8f)
                    curveToRelative(0f, 4.98f, 3.8f, 8.2f, 8f, 8.2f)
                    reflectiveCurveToRelative(8f, -3.22f, 8f, -8.2f)
                    curveTo(20f, 10.48f, 17.33f, 6.55f, 12f, 2f)
                    close()
                    moveTo(12f, 20f)
                    curveToRelative(-3.35f, 0f, -6f, -2.57f, -6f, -6.2f)
                    curveToRelative(0f, -2.34f, 1.95f, -5.44f, 6f, -9.14f)
                    curveToRelative(4.05f, 3.7f, 6f, 6.79f, 6f, 9.14f)
                    curveTo(18f, 17.43f, 15.35f, 20f, 12f, 20f)
                    close()
                    moveTo(7.83f, 14f)
                    curveToRelative(0.37f, 0f, 0.67f, 0.26f, 0.74f, 0.62f)
                    curveToRelative(0.41f, 2.22f, 2.28f, 2.98f, 3.64f, 2.87f)
                    curveToRelative(0.43f, -0.02f, 0.79f, 0.32f, 0.79f, 0.75f)
                    curveToRelative(0f, 0.4f, -0.32f, 0.73f, -0.72f, 0.75f)
                    curveToRelative(-2.13f, 0.13f, -4.62f, -1.09f, -5.19f, -4.12f)
                    curveTo(7.01f, 14.42f, 7.37f, 14f, 7.83f, 14f)
                    close()
                }
            }
        }.build()

        return _WaterDropWithBackground!!
    }

@Suppress("ObjectPropertyName")
private var _WaterDropWithBackground: ImageVector? = null