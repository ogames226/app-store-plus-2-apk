package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Touch-feedback primitives shared by every screen.
 *
 * Bare `Modifier.clickable` gives a ripple and nothing else, which reads as
 * "dead" on a card-sized surface. These helpers add a press-scale response and
 * enforce a 48dp minimum hit area so call sites don't each re-derive it.
 */

/** Android's minimum accessible touch target. */
val MinTouchTarget: Dp = 48.dp

/**
 * Clickable with a subtle press-scale response.
 *
 * Keep the [onClick] lambda stable at the call site (wrap in `remember { }`)
 * or this de-optimises every recomposition of the enclosing list item.
 */
@Composable
fun Modifier.pressable(
  enabled: Boolean = true,
  scaleOnPress: Float = 0.97f,
  onClick: () -> Unit
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed && enabled) scaleOnPress else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "pressScale"
  )
  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      enabled = enabled,
      interactionSource = interactionSource,
      indication = ripple(),
      onClick = onClick
    )
}

/** Grows a small visual element up to the 48dp minimum hit area. */
fun Modifier.minTouchTarget(size: Dp = MinTouchTarget): Modifier =
  defaultMinSize(minWidth = size, minHeight = size)

/**
 * Haptics are for *significant* events only — install start, download finish,
 * destructive confirm. Never on routine navigation or every list tap; that
 * reads as noise rather than feedback. `LongPress` is the one feedback type
 * guaranteed to be distinct on every API level this app supports.
 */
@Composable
fun rememberConfirmHaptic(): () -> Unit {
  val haptic = LocalHapticFeedback.current
  return remember(haptic) { { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } }
}
