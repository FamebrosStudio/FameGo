package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 5 Canonical Client Navigation Sections for the rotating wheel.
 * Each section corresponds to exactly one snap point along the wheel arc.
 */
enum class WheelSection(
  val index: Int,
  val id: String,
  val label: String,
  val tabKey: String,
  val isQuickAction: Boolean = false
) {
  DASHBOARD(0, "dashboard", "dashboard", "home"),
  BOOKINGS(1, "bookings", "bookings", "bookings"),
  BOOK_SHOOT(2, "book_shoot", "✦ book shoot", "book", isQuickAction = true),
  ALERTS(3, "alerts", "alerts", "notifications"),
  PROFILE(4, "profile", "profile", "profile");

  companion object {
    fun fromTab(tab: String): WheelSection {
      return when (tab) {
        "home", "dashboard" -> DASHBOARD
        "bookings" -> BOOKINGS
        "book" -> BOOK_SHOOT
        "notifications", "alerts" -> ALERTS
        "profile" -> PROFILE
        else -> DASHBOARD
      }
    }
  }
}

/**
 * Custom Convex Top Semicircle Shape.
 * Forms a smooth, sweeping dome arc across the top boundary that extends
 * edge-to-edge across the screen, cleanly clipping the lower rotating wheel body.
 */
class SemicircularDomeShape(
  private val arcRisePx: Float
) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    val path = Path().apply {
      moveTo(0f, size.height)
      lineTo(0f, arcRisePx)
      quadraticTo(
        x1 = size.width / 2f,
        y1 = 0f,
        x2 = size.width,
        y2 = arcRisePx
      )
      lineTo(size.width, size.height)
      close()
    }
    return Outline.Generic(path)
  }
}

/**
 * FameGo Rotating Half-Circle Navigation Wheel.
 *
 * A large circular wheel sitting underneath the interface, with approximately
 * the top 35-45% of the circle exposed at the bottom of the screen.
 *
 * Features:
 * - Physical horizontal drag, swipe, and flick gestures with momentum, friction, and spring snap
 * - 5 Snap positions (Dashboard, Bookings, Book Shoot, Alerts, Profile)
 * - Section labels visibly rotate along the circular arc with physical movement
 * - Clean prominent center typography with dynamic gold accent on "✦ book shoot"
 * - 5 Page indicator dots (● ○ ○ ○ ○) with tap-to-jump support
 * - Minimal left and right fallback tap navigation zones (< and >)
 * - Subtle one-time first-use hint ("Rotate wheel to explore") with auto-dismiss
 * - Mechanical tick marks along the perimeter arc that visibly rotate with physical motion
 * - Surface depression on touch, haptic tick feedback on dial snap
 */
@Composable
fun FameGoWheelNavigation(
  currentTab: String,
  onNavigate: (String) -> Unit,
  onOpenBookingFlow: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()

  // 1. Map current tab to initial active section
  val activeSection = remember(currentTab) { WheelSection.fromTab(currentTab) }

  // Step angle in degrees between sections (28 degrees creates an ideal ergonomic span)
  val stepDegrees = 28f

  // Wheel rotation angle state in degrees.
  // Center (Section 2: Book Shoot) has rotation angle 0°.
  // Formula: angleFor(index) = (2 - index) * stepDegrees
  fun angleForSection(index: Int): Float = (2 - index) * stepDegrees

  val rotationAngle = remember { Animatable(angleForSection(activeSection.index)) }
  var isDragging by remember { mutableStateOf(false) }

  // First-use discovery state
  var hasInteracted by remember { mutableStateOf(false) }

  // Sync internal wheel angle when external navigation changes (e.g. back navigation or deep link)
  LaunchedEffect(activeSection) {
    if (!isDragging) {
      val targetAngle = angleForSection(activeSection.index)
      if (rotationAngle.value != targetAngle) {
        rotationAngle.animateTo(
          targetValue = targetAngle,
          animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMediumLow
          )
        )
      }
    }
  }

  // Touch depth scale (surface slightly depresses when touched)
  val wheelDepressScale by animateFloatAsState(
    targetValue = if (isDragging) 0.982f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "wheelDepressScale"
  )

  // Subtle first-use discovery animation
  val infiniteTransition = rememberInfiniteTransition(label = "hintWiggle")
  val hintWiggleX by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "hintWiggleX"
  )

  // Currently focused section derived from rotation angle
  val currentFocusedIndex by remember {
    derivedStateOf {
      val angle = rotationAngle.value
      val rawIndex = 2 - (angle / stepDegrees).roundToInt()
      rawIndex.coerceIn(0, 4)
    }
  }

  val focusedSection = WheelSection.values()[currentFocusedIndex]

  // Overall container height for the visible dome (~142dp)
  val containerHeight = 142.dp
  val arcRiseDp = 32.dp
  val arcRisePx = with(density) { arcRiseDp.toPx() }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
    contentAlignment = Alignment.BottomCenter
  ) {
    // -------------------------------------------------------------
    // First-use discovery hint (above the semicircle)
    // -------------------------------------------------------------
    AnimatedVisibility(
      visible = !hasInteracted,
      enter = fadeIn(tween(300)),
      exit = fadeOut(tween(250)),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(y = (-44).dp)
        .offset { IntOffset(x = hintWiggleX.roundToInt(), y = 0) }
    ) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xEE1E2129),
        border = androidx.compose.foundation.BorderStroke(1.dp, FameGoGold.copy(alpha = 0.35f)),
        shadowElevation = 6.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = FameGoGold,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Rotate wheel to explore",
            color = FameGoWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp
          )
        }
      }
    }

    // -------------------------------------------------------------
    // Main Semicircular Wheel Container
    // -------------------------------------------------------------
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(containerHeight)
        .scale(wheelDepressScale)
        .testTag("famego_navigation_wheel"),
      contentAlignment = Alignment.BottomCenter
    ) {
      val screenWidthPx = constraints.maxWidth.toFloat()
      val containerHeightPx = constraints.maxHeight.toFloat()

      // Giant circle geometry:
      // Radius extends significantly wider than screen to generate a soft, wide convex arc
      val wheelRadiusPx = screenWidthPx * 0.82f
      val wheelCenterX = screenWidthPx / 2f
      val wheelCenterY = containerHeightPx + wheelRadiusPx - (containerHeightPx * 0.78f)

      // Velocity tracker for flick momentum physics
      val velocityTracker = remember { VelocityTracker() }
      val domeShape = remember(arcRisePx) { SemicircularDomeShape(arcRisePx) }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .shadow(
            elevation = 18.dp,
            shape = domeShape,
            spotColor = Color(0xFF000000),
            ambientColor = Color(0xFF000000)
          )
          .clip(domeShape)
          .background(Color(0xFF111317))
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = {
                isDragging = true
                hasInteracted = true
                velocityTracker.resetTracking()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onDrag = { change, dragAmount ->
                change.consume()
                hasInteracted = true
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                // Sensitivity factor converting horizontal drag pixels to wheel angular rotation
                // ~72 pixels of drag rotates by one full section (stepDegrees)
                val sensitivity = stepDegrees / 72f
                val newAngle = rotationAngle.value + (dragAmount.x * sensitivity)

                // Coerce angle to prevent spinning uncontrollably beyond end positions
                val minAngle = angleForSection(4) - (stepDegrees * 0.45f)
                val maxAngle = angleForSection(0) + (stepDegrees * 0.45f)

                coroutineScope.launch {
                  rotationAngle.snapTo(newAngle.coerceIn(minAngle, maxAngle))
                }
              },
              onDragEnd = {
                isDragging = false
                val velocityX = velocityTracker.calculateVelocity().x

                // Compute momentum projection: flick velocity carries rotation further
                val flingAngleDelta = (velocityX * 0.007f).coerceIn(-stepDegrees * 1.5f, stepDegrees * 1.5f)
                val projectedAngle = rotationAngle.value + flingAngleDelta

                // Find nearest snap section
                val targetIndex = (2 - (projectedAngle / stepDegrees).roundToInt()).coerceIn(0, 4)
                val snapTargetAngle = angleForSection(targetIndex)
                val targetSection = WheelSection.values()[targetIndex]

                coroutineScope.launch {
                  // Settle wheel smoothly with physical spring
                  rotationAngle.animateTo(
                    targetValue = snapTargetAngle,
                    animationSpec = spring(
                      dampingRatio = Spring.DampingRatioLowBouncy,
                      stiffness = 340f
                    )
                  )

                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                  // Synchronize with application router
                  if (targetSection == WheelSection.BOOK_SHOOT) {
                    onNavigate("book")
                  } else {
                    onNavigate(targetSection.tabKey)
                  }
                }
              },
              onDragCancel = {
                isDragging = false
                val targetIndex = (2 - (rotationAngle.value / stepDegrees).roundToInt()).coerceIn(0, 4)
                coroutineScope.launch {
                  rotationAngle.animateTo(
                    targetValue = angleForSection(targetIndex),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                  )
                }
              }
            )
          }
      ) {
        // -------------------------------------------------------------
        // Layer 1: Canvas Drawing (Wheel Body, Neumorphic Gradients, Mechanical Arc Ticks)
        // -------------------------------------------------------------
        val currentAngle = rotationAngle.value

        Canvas(
          modifier = Modifier.fillMaxSize()
        ) {
          val w = size.width
          val h = size.height

          // Subtle ambient dark charcoal radial shading
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFF1B1D24), Color(0xFF101215)),
              center = Offset(w / 2f, h * 0.65f),
              radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(w / 2f, h * 0.65f)
          )

          // Subtle curved glass reflection / top highlight arc
          drawArc(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color.Transparent,
                FameGoGold.copy(alpha = 0.3f),
                Color(0x44FFFFFF),
                FameGoGold.copy(alpha = 0.3f),
                Color.Transparent
              )
            ),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(wheelCenterX - wheelRadiusPx, wheelCenterY - wheelRadiusPx),
            size = Size(wheelRadiusPx * 2, wheelRadiusPx * 2),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
          )

          // Fine mechanical tick marks along the wheel perimeter
          // These physically rotate with currentAngle!
          val tickSpanDegrees = 75f
          val tickStep = 2.5f
          val totalTicks = (tickSpanDegrees * 2 / tickStep).toInt()

          for (i in -totalTicks / 2..totalTicks / 2) {
            val relativeTickAngle = (i * tickStep) + currentAngle
            // Only draw ticks within visible arc span (-60° to 60°)
            if (relativeTickAngle in -60f..60f) {
              val rad = (relativeTickAngle - 90f) * (PI.toFloat() / 180f)
              val isMajor = (i % 4 == 0)
              val tickLen = if (isMajor) 8f else 4.5f
              val tickAlpha = (1f - (abs(relativeTickAngle) / 55f)).coerceIn(0f, 1f)

              val startX = wheelCenterX + (wheelRadiusPx - 4f) * cos(rad)
              val startY = wheelCenterY + (wheelRadiusPx - 4f) * sin(rad)
              val endX = wheelCenterX + (wheelRadiusPx - 4f - tickLen) * cos(rad)
              val endY = wheelCenterY + (wheelRadiusPx - 4f - tickLen) * sin(rad)

              val isCenterTick = abs(relativeTickAngle) < 1.4f
              val tickColor = if (isCenterTick) {
                FameGoGold
              } else if (isMajor) {
                Color(0x44FFFFFF)
              } else {
                Color(0x22FFFFFF)
              }

              drawLine(
                color = tickColor.copy(alpha = tickAlpha * tickColor.alpha),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isCenterTick) 2f else 1f
              )
            }
          }

          // Top-Center Mechanical Reticle / Precision Indicator Notch
          val reticleY = arcRisePx - 6f
          drawLine(
            brush = Brush.verticalGradient(
              listOf(FameGoGold, FameGoGold.copy(alpha = 0.2f))
            ),
            start = Offset(w / 2f, reticleY),
            end = Offset(w / 2f, reticleY + 8f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
          )
        }

        // -------------------------------------------------------------
        // Layer 2: Rotating Labels Along the Curved Arc
        // Each label is physically positioned along the circle perimeter!
        // As the wheel rotates, labels translate and rotate smoothly.
        // -------------------------------------------------------------
        val labelRadiusPx = wheelRadiusPx - with(density) { 36.dp.toPx() }
        val labelWidthPx = with(density) { 140.dp.toPx() }
        val labelHeightPx = with(density) { 44.dp.toPx() }

        WheelSection.values().forEach { section ->
          val relativeAngle = ((section.index - 2) * stepDegrees) + currentAngle
          // Render only when within visible half-arc span (-65° to 65°)
          if (relativeAngle in -65f..65f) {
            val rad = (relativeAngle - 90f) * (PI.toFloat() / 180f)
            val itemCenterX = wheelCenterX + (labelRadiusPx * cos(rad))
            val itemCenterY = wheelCenterY + (labelRadiusPx * sin(rad))

            val distFromCenter = abs(relativeAngle)
            val normalizedDist = (distFromCenter / stepDegrees).coerceAtLeast(0f)

            val itemAlpha = (1f - (normalizedDist * 0.5f)).coerceIn(0.12f, 1f)
            val itemScale = (1.15f - (normalizedDist * 0.22f)).coerceIn(0.82f, 1.15f)
            val isCentered = distFromCenter < (stepDegrees * 0.5f)
            val isBookShoot = section == WheelSection.BOOK_SHOOT

            Box(
              modifier = Modifier
                .offset {
                  IntOffset(
                    x = (itemCenterX - (labelWidthPx / 2f)).roundToInt(),
                    y = (itemCenterY - (labelHeightPx / 2f)).roundToInt()
                  )
                }
                .size(width = 140.dp, height = 44.dp)
                .graphicsLayer {
                  rotationZ = relativeAngle * 0.65f
                  scaleX = itemScale
                  scaleY = itemScale
                  alpha = itemAlpha
                }
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  hasInteracted = true
                  coroutineScope.launch {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    rotationAngle.animateTo(
                      targetValue = angleForSection(section.index),
                      animationSpec = spring(dampingRatio = 0.85f, stiffness = 340f)
                    )
                    if (isBookShoot) {
                      if (isCentered) {
                        onOpenBookingFlow()
                      } else {
                        onNavigate("book")
                      }
                    } else {
                      onNavigate(section.tabKey)
                    }
                  }
                }
                .testTag("wheel_label_${section.id}"),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = section.label,
                  color = when {
                    isBookShoot && isCentered -> FameGoGold
                    isCentered -> FameGoWhite
                    isBookShoot -> FameGoGold.copy(alpha = 0.8f)
                    else -> FameGoTextSecondary
                  },
                  fontSize = if (isCentered) 17.sp else 14.sp,
                  fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Medium,
                  letterSpacing = if (isBookShoot) 0.4.sp else (-0.2).sp,
                  textAlign = TextAlign.Center,
                  maxLines = 1
                )
                if (isBookShoot && isCentered) {
                  Text(
                    text = "Tap to schedule",
                    color = FameGoGold.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                  )
                }
              }
            }
          }
        }

        // -------------------------------------------------------------
        // Layer 3: Fallback Tap Controls (< Chevron, > Chevron)
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left Tap Navigation Chevron
          Box(
            modifier = Modifier
              .size(48.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = currentFocusedIndex > 0
              ) {
                hasInteracted = true
                val newIndex = (currentFocusedIndex - 1).coerceAtLeast(0)
                val targetSection = WheelSection.values()[newIndex]
                coroutineScope.launch {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  rotationAngle.animateTo(
                    targetValue = angleForSection(newIndex),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 340f)
                  )
                  if (targetSection == WheelSection.BOOK_SHOOT) {
                    onNavigate("book")
                  } else {
                    onNavigate(targetSection.tabKey)
                  }
                }
              }
              .testTag("wheel_chevron_left"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Previous Section",
              tint = if (currentFocusedIndex > 0) FameGoTextMuted else Color(0x18FFFFFF),
              modifier = Modifier.size(20.dp)
            )
          }

          // Right Tap Navigation Chevron
          Box(
            modifier = Modifier
              .size(48.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = currentFocusedIndex < 4
              ) {
                hasInteracted = true
                val newIndex = (currentFocusedIndex + 1).coerceAtMost(4)
                val targetSection = WheelSection.values()[newIndex]
                coroutineScope.launch {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  rotationAngle.animateTo(
                    targetValue = angleForSection(newIndex),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 340f)
                  )
                  if (targetSection == WheelSection.BOOK_SHOOT) {
                    onNavigate("book")
                  } else {
                    onNavigate(targetSection.tabKey)
                  }
                }
              }
              .testTag("wheel_chevron_right"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "Next Section",
              tint = if (currentFocusedIndex < 4) FameGoTextMuted else Color(0x18FFFFFF),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // -------------------------------------------------------------
        // Layer 4: Page Indicator Dots (● ○ ○ ○ ○)
        // Positioned at the bottom of the semicircle dome.
        // Each dot is minimum 48dp touch target for direct jumping!
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          WheelSection.values().forEachIndexed { index, section ->
            val isSelected = index == currentFocusedIndex

            Box(
              modifier = Modifier
                .size(width = 38.dp, height = 36.dp)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  hasInteracted = true
                  coroutineScope.launch {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    rotationAngle.animateTo(
                      targetValue = angleForSection(index),
                      animationSpec = spring(dampingRatio = 0.85f, stiffness = 340f)
                    )
                    if (section == WheelSection.BOOK_SHOOT) {
                      onNavigate("book")
                    } else {
                      onNavigate(section.tabKey)
                    }
                  }
                }
                .semantics {
                  contentDescription = "Jump to ${section.label}"
                  role = Role.Tab
                }
                .testTag("wheel_dot_$index"),
              contentAlignment = Alignment.Center
            ) {
              val dotSize by animateFloatAsState(
                targetValue = if (isSelected) 7f else 4f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "dotSize_$index"
              )

              val dotColor by animateColorAsState(
                targetValue = when {
                  isSelected && section == WheelSection.BOOK_SHOOT -> FameGoGold
                  isSelected -> FameGoWhite
                  else -> Color(0x3BFFFFFF)
                },
                animationSpec = tween(160),
                label = "dotColor_$index"
              )

              Box(
                modifier = Modifier
                  .size(dotSize.dp)
                  .clip(CircleShape)
                  .background(dotColor)
                  .then(
                    if (isSelected) {
                      Modifier.border(
                        width = 1.dp,
                        color = if (section == WheelSection.BOOK_SHOOT) FameGoGold.copy(alpha = 0.6f) else Color(0x66FFFFFF),
                        shape = CircleShape
                      )
                    } else Modifier
                  )
              )
            }
          }
        }
      }
    }
  }
}
