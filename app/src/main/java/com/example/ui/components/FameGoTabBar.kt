package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import dev.chrisbanes.haze.hazeChild
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.FameGoBrightGold
import com.example.ui.theme.FameGoGlassBg
import com.example.ui.theme.FameGoGlassBorder
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoWhite
import kotlin.math.roundToInt

/**
 * A bottom tab entry: outline icon at rest, filled icon when selected —
 * the classic minimal tab bar, in FameGo gold.
 */
data class FameGoTab(
  val route: String,
  val label: String,
  val outlineIcon: ImageVector,
  val filledIcon: ImageVector,
  /** Signature accent (the center Book tab stays gold even at rest). */
  val accent: Boolean = false,
  val testTag: String = "tab_$route"
)

fun fameGoClientTabs() = listOf(
  FameGoTab("home", "Home", Icons.Outlined.Home, Icons.Filled.Home, testTag = "tab_home"),
  FameGoTab(
    "bookings", "Bookings",
    Icons.Outlined.ConfirmationNumber, Icons.Filled.ConfirmationNumber,
    testTag = "tab_bookings"
  ),
  FameGoTab("book", "Book", Icons.Filled.Add, Icons.Filled.Add, accent = true, testTag = "tab_book"),
  FameGoTab(
    "notifications", "Alerts",
    Icons.Outlined.Notifications, Icons.Filled.Notifications,
    testTag = "tab_notifications"
  ),
  FameGoTab(
    "profile", "Profile",
    Icons.Outlined.Person, Icons.Filled.Person,
    testTag = "tab_profile"
  )
)

fun fameGoCrewTabs() = listOf(
  FameGoTab("requests", "Requests", Icons.Outlined.ConfirmationNumber, Icons.Filled.ConfirmationNumber, testTag = "tab_requests"),
  FameGoTab(
    "bookings", "Shoots",
    Icons.Outlined.Home, Icons.Filled.Home,
    testTag = "tab_bookings"
  ),
  FameGoTab(
    "notifications", "Alerts",
    Icons.Outlined.Notifications, Icons.Filled.Notifications,
    testTag = "tab_notifications"
  ),
  FameGoTab(
    "profile", "Profile",
    Icons.Outlined.Person, Icons.Filled.Person,
    testTag = "tab_profile"
  )
)

fun fameGoAdminTabs() = listOf(
  FameGoTab("dashboard", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home, testTag = "tab_dashboard"),
  FameGoTab(
    "users", "People",
    Icons.Outlined.Person, Icons.Filled.Person,
    testTag = "tab_users"
  ),
  FameGoTab(
    "notifications", "Alerts",
    Icons.Outlined.Notifications, Icons.Filled.Notifications,
    testTag = "tab_notifications"
  ),
  FameGoTab(
    "profile", "Profile",
    Icons.Outlined.Person, Icons.Outlined.Person,
    testTag = "tab_profile"
  )
)

/**
 * FameGo bottom tab bar — dark floating glass pill with a spring-loaded
 * gold capsule that glides behind the active tab. The yellow never jumps:
 * one shared indicator animates across tabs on a soft spring, the active
 * icon pops gently and tints gold, labels stay put for readability.
 */
@Composable
fun FameGoTabBar(
  tabs: List<FameGoTab>,
  selectedRoute: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: dev.chrisbanes.haze.HazeState? = null
) {
  val haptic = LocalHapticFeedback.current
  val density = LocalDensity.current
  val tabSfx = LocalContext.current.applicationContext
  // Center-x of every tab cell, measured after layout. Reset with the tab
  // list so stale positions can never fling the capsule across the bar.
  var centers by remember(tabs) { mutableStateOf(mapOf<String, Float>()) }
  val pillWidthPx = remember(density) { with(density) { 60.dp.toPx() } }
  val targetCenter = centers[selectedRoute]

  // One shared indicator: glides on a soft under-damped spring, interruptible
  // mid-flight when taps come fast. Snaps on first measure, never animates
  // from a stale position.
  val glideX = remember(tabs) { Animatable(0f) }
  var glideReady by remember(tabs) { mutableStateOf(false) }
  LaunchedEffect(targetCenter) {
    if (targetCenter != null) {
      if (!glideReady) {
        glideX.snapTo(targetCenter)
        glideReady = true
      } else {
        glideX.animateTo(targetCenter, FameGoSprings.settle())
      }
    }
  }
  // Squash & stretch: the capsule smushes along its travel direction with
  // speed, then relaxes back to round. Reads animation velocity per frame.
  val speed = kotlin.math.abs(glideX.velocity)
  val smush = 1f + (speed / 5000f).coerceAtMost(0.28f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("famego_tab_bar"),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = if (hazeState != null) Color.Transparent else FameGoGlassBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, FameGoGlassBorder),
      shadowElevation = 10.dp,
      modifier = Modifier.then(
        if (hazeState != null) {
          Modifier.hazeChild(
            state = hazeState,
            style = dev.chrisbanes.haze.HazeDefaults.style(
              backgroundColor = Color(0xFF0B0D10).copy(alpha = 0.55f),
              tint = dev.chrisbanes.haze.HazeTint(Color.White.copy(alpha = 0.04f)),
              blurRadius = 26.dp,
              noiseFactor = 0f
            )
          )
        } else Modifier
      )
    ) {
      Box {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          tabs.forEach { tab ->
            val selected = tab.route == selectedRoute
            FameGoTabItem(
              tab = tab,
              selected = selected,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                com.example.data.FameGoSfx.click(tabSfx)
                onSelect(tab.route)
              },
              modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coords ->
                  val center = coords.positionInParent().x + coords.size.width / 2f
                  val prev = centers[tab.route]
                  if (prev == null || kotlin.math.abs(prev - center) > 1f) {
                    centers = centers + (tab.route to center)
                  }
                }
            )
          }
        }
        // The traveling gold capsule — visible once measured, slides forever.
        if (glideReady && targetCenter != null) {
          Box(
            modifier = Modifier
              .align(Alignment.CenterStart)
              .offset {
                IntOffset(
                  (glideX.value - pillWidthPx / 2f).roundToInt(),
                  0
                )
              }
              .graphicsLayer {
                scaleX = smush
                scaleY = 1f - (smush - 1f) * 0.55f
              }
              .width(60.dp)
              .height(44.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(FameGoGold, FameGoBrightGold, FameGoGold)
                )
              ),
          )
        }
        // Hairline glass highlight along the top edge (reference look).
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .size(height = 1.dp, width = 10.dp)
            .align(Alignment.TopCenter)
        ) {
          drawLine(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.35f),
                Color.Transparent
              )
            ),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height
          )
        }
      }
    }
  }
}

@Composable
private fun FameGoTabItem(
  tab: FameGoTab,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  // Gentle pop when a tab becomes active; subtle press-down under finger.
  val pop by animateFloatAsState(
    targetValue = when {
      pressed -> 0.9f
      selected -> 1.12f
      else -> 1f
    },
    animationSpec = FameGoSprings.pop(),
    label = "tabPop"
  )
  val iconTint by animateColorAsState(
    targetValue = if (selected) Color(0xFF1A1408) else FameGoTextMuted,
    animationSpec = tween(200),
    label = "tabIconTint"
  )

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
      )
      .semantics {
        contentDescription = tab.label
        role = Role.Tab
        this.selected = selected
      }
      .testTag(tab.testTag)
      .padding(vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = if (selected) tab.filledIcon else tab.outlineIcon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier
        .size(24.dp)
        .scale(pop)
    )
    Text(
      text = tab.label,
      color = if (selected) FameGoWhite else FameGoTextMuted,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      letterSpacing = 0.1.sp,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 3.dp)
    )
  }
}
