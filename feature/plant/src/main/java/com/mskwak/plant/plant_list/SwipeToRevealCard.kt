package com.mskwak.plant.plant_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

enum class RevealState {
    CLOSED, OPENED
}

/**
 * @param actionWidth 드러날 너비
 * @return [AnchoredDraggableState]
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSwipeToRevealState(
    actionWidth: Dp
): AnchoredDraggableState<RevealState> {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.toPx() }
    return remember(actionWidth) {
        AnchoredDraggableState(
            initialValue = RevealState.CLOSED,
            anchors = DraggableAnchors {
                RevealState.CLOSED at 0f
                RevealState.OPENED at -actionWidthPx
            }
        )
    }
}

/**
 * @param state [AnchoredDraggableState] created by [rememberSwipeToRevealState].
 * @param backgroundContent 드러날 버튼의 컨텐츠(뒷면)
 * @param cardContent 카드의 컨텐츠(앞면)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToRevealCard(
    state: AnchoredDraggableState<RevealState>,
    modifier: Modifier = Modifier,
    backgroundContent: @Composable BoxScope.() -> Unit,
    cardContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.align(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
            backgroundContent()
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = if (state.requireOffset().isNaN()) {
                            0
                        } else {
                            state.requireOffset().roundToInt()
                        },
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
                )
        ) {
            cardContent()
        }
    }
}
