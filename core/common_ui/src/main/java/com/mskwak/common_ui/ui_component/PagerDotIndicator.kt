package com.mskwak.common_ui.ui_component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PagerDotIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 6.dp,
) {
    val dotSizePx = with(LocalDensity.current) { dotSize.toPx() }
    val dotSpacingPx = with(LocalDensity.current) { dotSpacing.toPx() }
    val activeOffsetX = with(LocalDensity.current) {
        ((pagerState.currentPage + pagerState.currentPageOffsetFraction)
                        * (dotSizePx + dotSpacingPx)).toDp()
    }

    Box(
        modifier = modifier, contentAlignment = Alignment.CenterStart
    ) {
        // 비활성 dot 전체
        Row(
            horizontalArrangement = Arrangement.spacedBy(dotSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }

        // 슬라이딩 활성 dot
        Box(
            modifier = Modifier
                .offset(x = activeOffsetX)
                .size(dotSize)
                .background(
                    color = MaterialTheme.colorScheme.primary, shape = CircleShape
                )
        )
    }
}
