package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoLiveRed
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Contextual central action type based on active booking state.
 */
enum class HalfRingCenterContext(
  val label: String,
  val icon: ImageVector,
  val badgeColor: Color = FameGoGold
) {
  BOOK("Book", Icons.Default.Add, FameGoGold),
  SEARCHING("Finding", Icons.Default.Radar, FameGoGold),
  VIEW_SHOOT("Shoot", Icons.Default.Videocam, FameGoAccentCyan),
  LIVE("Live", Icons.Default.CameraAlt, FameGoLiveRed),
  BOOK_AGAIN("Book Again", Icons.Default.Refresh, FameGoGold)
}

val FameGoAccentCyan = Color(0xFF00E5FF)

/**
 * Navigation items presented on the FameGo Half Ring arc.
 */
enum class HalfRingItem(
  val id: String,
  val label: String,
  val icon: ImageVector,
  val targetTab: String
) {
  NOW("now", "Now", Icons.Default.CameraAlt, "home"),
  SHOOTS("shoots", "Shoots", Icons.Default.DateRange, "bookings"),
  CENTER("center", "Action", Icons.Default.Add, "book"),
  ALERTS("alerts", "Alerts", Icons.Default.Notifications, "notifications"),
  PROFILE("profile", "Profile", Icons.Default.Person, "profile")
}

/**
 * FameGo Half Ring
 *
 * A tactile semi-circular interactive dashboard control placed around the lower
 * portion of the screen. Approximately 65% of the circular dial sits below the
 * screen boundary, exposing an arc with 5 snap points.
 *
 * Features:
 * - Fluid radial drag gestures with friction and spring snap settle
 * - Direct tap to rotate directly to target
 * - Dynamic context-aware center action (BOOK, SEARCHING, VIEW SHOOT, LIVE, BOOK AGAIN)
 * - Focused item scales up with animated label and subtle haptic feedback
 * - Minimum 48dp touch targets and accessibility semantics
 */
@Composable
fun FameGoHalfRing(
  currentTab: String,
  onNavigate: (String) -> Unit,
  onTriggerCenterAction: () -> Unit,
  centerContext: HalfRingCenterContext = HalfRingCenterContext.BOOK,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  // Items mapped to positions 0, 1, 2 (Center), 3, 4
  val items = remember {
    listOf(
      HalfRingItem.NOW,
      HalfRingItem.SHOOTS,
      HalfRingItem.CENTER,
      HalfRingItem.ALERTS,
      HalfRingItem.PROFILE
    )
  }

  // Derive initial selected index from currentTab
  val initialIndex = when (currentTab) {
    "home" -> 0
    "bookings" -> 1
    "notifications" -> 3
    "profile" -> 4
    else -> 0
  }

  var selectedIndex by remember { mutableIntStateOf(initialIndex) }
  val rotationOffset = remember { Animatable(0f) }
  var isDragging by remember { mutableStateOf(false) }

  // Sync selected index when external tab changes
  LaunchedEffect(currentTab) {
    val targetIndex = when (currentTab) {
      "home" -> 0
      "bookings" -> 1
      "notifications" -> 3
      "profile" -> 4
      else -> selectedIndex
    }
    if (targetIndex != selectedIndex && !isDragging) {
      selectedIndex = targetIndex
    }
  }

  // Touch depth scale (slightly pressed when dragging)
  val dialScale by animateFloatAsState(
    targetValue = if (isDragging) 0.985f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "dialScale"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(bottom = 6.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Arc Container
    Box(
      modifier = Modifier
        .size(width = 340.dp, height = 112.dp)
        .scale(dialScale)
        .pointerInput(Unit) {
          detectHorizontalDragGestures(
            onDragStart = {
              isDragging = true
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            onDragEnd = {
              isDragging = false
              // Snap to nearest item based on drag offset
              val currentOffset = rotationOffset.value
              val stepSize = 44f
              val nearestShift = (-currentOffset / stepSize).roundToInt()
              val newIndex = (selectedIndex + nearestShift).coerceIn(0, items.size - 1)

              coroutineScope.launch {
                rotationOffset.animateTo(
                  targetValue = 0f,
                  animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                  )
                )
                if (newIndex != selectedIndex) {
                  selectedIndex = newIndex
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  val selected = items[selectedIndex]
                  if (selected == HalfRingItem.CENTER) {
                    onTriggerCenterAction()
                  } else {
                    onNavigate(selected.targetTab)
                  }
                }
              }
            },
            onDragCancel = {
              isDragging = false
              coroutineScope.launch {
                rotationOffset.animateTo(0f)
              }
            },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              coroutineScope.launch {
                rotationOffset.snapTo(
                  (rotationOffset.value + dragAmount * 0.8f).coerceIn(-90f, 90f)
                )
              }
            }
          )
        },
      contentAlignment = Alignment.BottomCenter
    ) {
      // 1. Soft-Neumorphic Half-Ring Canvas Base
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(112.dp)
      ) {
        val w = size.width
        val h = size.height

        // Dial Arch (Upper curve of circle resting below screen)
        val arcCenter = Offset(w / 2f, h + 130f)
        val arcRadius = 210f

        // Soft background ambient glow
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0x221E2026), Color.Transparent),
            center = Offset(w / 2f, h * 0.6f),
            radius = 180f
          ),
          radius = 180f,
          center = Offset(w / 2f, h * 0.6f)
        )

        // Outer fine highlight ring
        drawArc(
          color = Color(0x33FFFFFF),
          startAngle = 195f,
          sweepAngle = 150f,
          useCenter = false,
          topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
          size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
          style = Stroke(width = 1.2f, cap = StrokeCap.Round)
        )

        // Inner soft neumorphic body curve
        drawArc(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1C1E24), Color(0xFF121316))
          ),
          startAngle = 190f,
          sweepAngle = 160f,
          useCenter = true,
          topLeft = Offset(arcCenter.x - arcRadius + 4f, arcCenter.y - arcRadius + 4f),
          size = androidx.compose.ui.geometry.Size((arcRadius - 4f) * 2, (arcRadius - 4f) * 2)
        )
      }

      // 2. Arc Navigation Items Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp)
          .offset { IntOffset(x = rotationOffset.value.roundToInt(), y = 0) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        items.forEachIndexed { index, item ->
          val isCenter = item == HalfRingItem.CENTER
          val isSelected = selectedIndex == index

          HalfRingItemSlot(
            item = item,
            isSelected = isSelected,
            isCenter = isCenter,
            centerContext = centerContext,
            onClick = {
              selectedIndex = index
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              if (isCenter) {
                onTriggerCenterAction()
              } else {
                onNavigate(item.targetTab)
              }
            }
          )
        }
      }
    }
  }
}

/**
 * Individual Interactive Slot along the Half Ring.
 * Ensures touch target >= 48dp with tactile scaling.
 */
@Composable
private fun HalfRingItemSlot(
  item: HalfRingItem,
  isSelected: Boolean,
  isCenter: Boolean,
  centerContext: HalfRingCenterContext,
  onClick: () -> Unit
) {
  val icon = if (isCenter) centerContext.icon else item.icon
  val label = if (isCenter) centerContext.label else item.label

  val scale by animateFloatAsState(
    targetValue = if (isCenter) (if (isSelected) 1.15f else 1.08f) else (if (isSelected) 1.18f else 0.95f),
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "slotScale"
  )

  val iconColor by animateColorAsState(
    targetValue = when {
      isCenter -> Color(0xFF141518)
      isSelected -> FameGoGold
      else -> FameGoTextMuted
    },
    animationSpec = tween(160),
    label = "slotIconColor"
  )

  val buttonBg = when {
    isCenter -> Brush.horizontalGradient(listOf(FameGoGold, Color(0xFFE5C058)))
    isSelected -> Brush.radialGradient(listOf(Color(0xFF2E313A), Color(0xFF1F2127)))
    else -> Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
  }

  Column(
    modifier = Modifier
      .size(width = if (isCenter) 64.dp else 52.dp, height = 76.dp)
      .scale(scale)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .semantics {
        contentDescription = "FameGo navigation: $label"
        role = Role.Tab
      }
      .testTag("half_ring_${item.id}"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom
  ) {
    // Icon Pill / Button
    Box(
      modifier = Modifier
        .size(if (isCenter) 52.dp else 44.dp)
        .clip(CircleShape)
        .background(buttonBg)
        .border(
          width = 1.dp,
          color = when {
            isCenter -> FameGoGold.copy(alpha = 0.8f)
            isSelected -> FameGoGold.copy(alpha = 0.4f)
            else -> Color(0x18FFFFFF)
          },
          shape = CircleShape
        )
        .shadow(
          elevation = if (isCenter) 8.dp else 2.dp,
          shape = CircleShape,
          spotColor = if (isCenter) FameGoGold else Color.Black
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(if (isCenter) 24.dp else 20.dp)
      )
    }

    Spacer(modifier = Modifier.height(3.dp))

    // Animated Label (Visible when selected or for center action)
    AnimatedContent(
      targetState = isSelected || isCenter,
      transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(100)) },
      label = "labelVisibility"
    ) { visible ->
      if (visible) {
        Text(
          text = label,
          color = if (isCenter) FameGoGold else if (isSelected) FameGoWhite else FameGoTextMuted,
          fontSize = 10.sp,
          fontWeight = if (isSelected || isCenter) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1
        )
      } else {
        // Subtle indicator dot for inactive items
        Box(
          modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
        )
      }
    }
  }
}
