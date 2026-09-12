package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.CrewProfile
import com.example.model.Role
import com.example.model.VerificationStatus
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoBrightGold
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoDarkerGold
import com.example.ui.theme.FameGoGlassBg
import com.example.ui.theme.FameGoGlassBorder
import com.example.ui.theme.FameGoGlassHighlight
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoLiveRed
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import com.example.R
import kotlin.math.roundToInt

// =============================================================================
// 1. THE FLOW PILL (Signature interactive pill morphing near bottom of screen)
// =============================================================================

enum class FlowPillState {
  BOOK_A_SHOOT,
  CONTINUE,
  FINDING_CREW,
  CREW_CONFIRMED,
  SHOOT_IN_PROGRESS,
  BOOK_AGAIN,
  CUSTOM
}

@Composable
fun FlowPill(
  state: FlowPillState = FlowPillState.BOOK_A_SHOOT,
  customText: String? = null,
  customIcon: ImageVector? = null,
  onClick: () -> Unit,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
  testTag: String = "flow_pill"
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1.0f,
    animationSpec = spring(stiffness = 500f),
    label = "flowPillScale"
  )

  val (text, icon, isGold, isLoading) = when (state) {
    FlowPillState.BOOK_A_SHOOT -> Quadruple(customText ?: "Book a shoot", Icons.Default.Add, true, false)
    FlowPillState.CONTINUE -> Quadruple(customText ?: "Continue", Icons.AutoMirrored.Filled.ArrowForward, true, false)
    FlowPillState.FINDING_CREW -> Quadruple(customText ?: "Finding crew...", null, false, true)
    FlowPillState.CREW_CONFIRMED -> Quadruple(customText ?: "Crew confirmed", Icons.Default.Check, false, false)
    FlowPillState.SHOOT_IN_PROGRESS -> Quadruple(customText ?: "Shoot in progress", null, false, false)
    FlowPillState.BOOK_AGAIN -> Quadruple(customText ?: "Book again", Icons.Default.Refresh, true, false)
    FlowPillState.CUSTOM -> Quadruple(customText ?: "Confirm", customIcon, true, false)
  }

  val backgroundColor by animateColorAsState(
    targetValue = when {
      !enabled -> FameGoCard
      isGold -> FameGoGold
      state == FlowPillState.CREW_CONFIRMED -> Color(0xFF142E20)
      state == FlowPillState.SHOOT_IN_PROGRESS -> Color(0xFF1F1B12)
      else -> FameGoCardElevated
    },
    animationSpec = tween(250),
    label = "pillBgColor"
  )

  val contentColor by animateColorAsState(
    targetValue = when {
      !enabled -> FameGoTextMuted
      isGold -> FameGoBg
      state == FlowPillState.CREW_CONFIRMED -> FameGoSuccessGreen
      state == FlowPillState.SHOOT_IN_PROGRESS -> FameGoGold
      else -> FameGoTextPrimary
    },
    animationSpec = tween(250),
    label = "pillContentColor"
  )

  Surface(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(32.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled && !isLoading,
        onClick = onClick
      )
      .testTag(testTag),
    shape = RoundedCornerShape(32.dp),
    color = backgroundColor,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      when {
        isGold -> FameGoBrightGold.copy(alpha = 0.5f)
        state == FlowPillState.CREW_CONFIRMED -> FameGoSuccessGreen.copy(alpha = 0.4f)
        state == FlowPillState.SHOOT_IN_PROGRESS -> FameGoGold.copy(alpha = 0.4f)
        else -> FameGoBorderSubtle
      }
    ),
    shadowElevation = if (isGold) 6.dp else 2.dp
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 24.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isLoading) {
        LiveOrb(color = FameGoGold, size = 10.dp)
        Spacer(modifier = Modifier.width(10.dp))
      } else if (state == FlowPillState.SHOOT_IN_PROGRESS) {
        LiveOrb(color = FameGoGold, size = 8.dp)
        Spacer(modifier = Modifier.width(10.dp))
      } else if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
      }

      Text(
        text = text,
        color = contentColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp
      )
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// =============================================================================
// 2. THE FLOW DOCK (Floating Soft-Glass Navigation Dock)
// =============================================================================

@Composable
fun FlowDock(
  currentRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    // Level 3 Floating Glass Dock
    Surface(
      shape = RoundedCornerShape(36.dp),
      color = FameGoGlassBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, FameGoGlassBorder),
      shadowElevation = 8.dp
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Home
        DockTabItem(
          selected = currentRoute == "home",
          icon = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
          label = "Home",
          testTag = "nav_home",
          onClick = { onNavigate("home") }
        )

        // Bookings
        DockTabItem(
          selected = currentRoute == "bookings",
          icon = Icons.Outlined.ConfirmationNumber,
          label = "Bookings",
          testTag = "nav_bookings",
          onClick = { onNavigate("bookings") }
        )

        // Center + Flow Action Button (FameGo Gold)
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(FameGoGold)
            .clickable { onNavigate("book") }
            .testTag("nav_book_a_shoot"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Book a shoot",
            tint = FameGoBg,
            modifier = Modifier.size(22.dp)
          )
        }

        // Alerts / Notifications
        DockTabItem(
          selected = currentRoute == "notifications",
          icon = if (currentRoute == "notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications,
          label = "Alerts",
          testTag = "nav_notifications",
          onClick = { onNavigate("notifications") }
        )

        // Profile
        DockTabItem(
          selected = currentRoute == "profile",
          icon = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
          label = "Profile",
          testTag = "nav_profile",
          onClick = { onNavigate("profile") }
        )
      }
    }
  }
}

@Composable
private fun DockTabItem(
  selected: Boolean,
  icon: ImageVector,
  label: String,
  testTag: String,
  onClick: () -> Unit
) {
  val iconColor by animateColorAsState(
    targetValue = if (selected) FameGoWhite else FameGoTextMuted,
    animationSpec = tween(150),
    label = "tabIconColor"
  )

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = iconColor,
      modifier = Modifier.size(20.dp)
    )
  }
}

// Backward-compatible alias
@Composable
fun FameGoBottomNav(
  currentRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  FlowDock(currentRoute = currentRoute, onNavigate = onNavigate, modifier = modifier)
}

// =============================================================================
// 3. ADAPTIVE HEADER (Clean, Minimalist Top Area)
// =============================================================================

@Composable
fun FameGoLogo(
  modifier: Modifier = Modifier,
  contentDescription: String = "FameGo"
) {
  androidx.compose.foundation.Image(
    painter = painterResource(R.drawable.famego_logo),
    contentDescription = contentDescription,
    contentScale = ContentScale.Fit,
    modifier = modifier.heightIn(min = 28.dp)
  )
}

@Composable
fun FameGoWordmark(modifier: Modifier = Modifier) {
  Text(
    text = "FameGo",
    color = FameGoWhite,
    fontFamily = FontFamily(Font(R.font.megrim_regular)),
    fontSize = 20.sp,
    letterSpacing = 0.4.sp,
    modifier = modifier
  )
}

@Composable
fun AdaptiveHeader(
  currentRole: Role,
  unreadNotifications: Int = 0,
  onRoleClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  roleSwitcherEnabled: Boolean = false,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    FameGoWordmark(modifier = Modifier.width(92.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Role Switcher capsule (Subtle Dev/Demo affordance)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = FameGoCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
        modifier = Modifier
          .clickable(enabled = roleSwitcherEnabled) { onRoleClick() }
          .testTag("role_switcher_chip")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(
                when (currentRole) {
                  Role.CLIENT -> FameGoGold
                  Role.CREW -> FameGoSuccessGreen
                  Role.ADMIN -> FameGoAccentCyan
                }
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = when (currentRole) {
              Role.CLIENT -> "Client"
              Role.CREW -> "Crew"
              Role.ADMIN -> "Admin"
            },
            color = FameGoTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Notifications button with unread badge
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(FameGoCard)
          .border(1.dp, FameGoBorderSubtle, CircleShape)
          .clickable { onNotificationsClick() }
          .testTag("top_bar_notifications_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Notifications",
          tint = FameGoTextSecondary,
          modifier = Modifier.size(16.dp)
        )
        if (unreadNotifications > 0) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .size(14.dp)
              .clip(CircleShape)
              .background(FameGoGold),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (unreadNotifications > 9) "9+" else "$unreadNotifications",
              color = FameGoBg,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Profile Avatar button
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(FameGoCard)
          .border(1.dp, FameGoBorderSubtle, CircleShape)
          .clickable { onProfileClick() }
          .testTag("top_bar_profile_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Profile",
          tint = FameGoTextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

// Backward-compatible alias
@Composable
fun FameGoTopBar(
  title: String = "FameGo",
  subtitle: String = "by Famebros Studio",
  currentRole: Role,
  unreadNotifications: Int = 0,
  onRoleClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  roleSwitcherEnabled: Boolean = false,
  modifier: Modifier = Modifier
) {
  AdaptiveHeader(
    currentRole = currentRole,
    unreadNotifications = unreadNotifications,
    onRoleClick = onRoleClick,
    onNotificationsClick = onNotificationsClick,
    onProfileClick = onProfileClick,
    roleSwitcherEnabled = roleSwitcherEnabled,
    modifier = modifier
  )
}

// =============================================================================
// 4. SOFT CARD (Level 2 Neumorphic Surface)
// =============================================================================

@Composable
fun SoftCard(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(20.dp),
  isElevated: Boolean = false,
  onClick: (() -> Unit)? = null,
  testTag: String? = null,
  content: @Composable () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
    animationSpec = spring(stiffness = 500f),
    label = "softCardScale"
  )

  Surface(
    modifier = modifier
      .scale(scale)
      .clip(shape)
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
          )
        } else Modifier
      )
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    shape = shape,
    color = if (isElevated) FameGoCardElevated else FameGoCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
    shadowElevation = if (isElevated) 3.dp else 1.dp
  ) {
    content()
  }
}

// =============================================================================
// 5. LIVE ORB (Gentle Pulsing Status Indicator)
// =============================================================================

@Composable
fun LiveOrb(
  color: Color = FameGoSuccessGreen,
  size: Dp = 8.dp,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbAlpha"
  )
  val haloScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.8f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbHaloScale"
  )

  Box(
    modifier = modifier.size(size * 2),
    contentAlignment = Alignment.Center
  ) {
    // Subtle outer halo
    Box(
      modifier = Modifier
        .size(size)
        .scale(haloScale)
        .clip(CircleShape)
        .background(color.copy(alpha = alpha * 0.25f))
    )
    // Core orb
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(color)
    )
  }
}

// =============================================================================
// 6. ROLE COUNTER (Stacked Tactile Neumorphic Controls)
// =============================================================================

@Composable
fun RoleCounter(
  roleTitle: String,
  roleSubtitle: String,
  ratePerHour: Int,
  count: Int,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  modifier: Modifier = Modifier,
  testTagPrefix: String = "counter"
) {
  val isSelected = count > 0

  val animatedBg by animateColorAsState(
    targetValue = if (isSelected) FameGoCardElevated else FameGoCard,
    animationSpec = tween(200),
    label = "roleCounterBg"
  )
  val animatedBorder by animateColorAsState(
    targetValue = if (isSelected) FameGoGold.copy(alpha = 0.35f) else FameGoBorderSubtle,
    animationSpec = tween(200),
    label = "roleCounterBorder"
  )

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = animatedBg,
    border = androidx.compose.foundation.BorderStroke(1.dp, animatedBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = roleTitle,
            color = if (isSelected) FameGoWhite else FameGoTextPrimary,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
          )
          if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(FameGoGold)
            )
          }
        }
        Text(
          text = "₹$ratePerHour/hr • $roleSubtitle",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      // Tactile [ - ] [ Count ] [ + ] Controls
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Minus
        TactileCircleButton(
          icon = Icons.Default.Remove,
          enabled = count > 0,
          onClick = onDecrement,
          testTag = "${testTagPrefix}_decrement"
        )

        // Count number
        Text(
          text = count.toString(),
          color = if (isSelected) FameGoGold else FameGoTextMuted,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.width(24.dp),
          textAlign = TextAlign.Center
        )

        // Plus
        TactileCircleButton(
          icon = Icons.Default.Add,
          enabled = count < 5,
          isHighlight = isSelected,
          onClick = onIncrement,
          testTag = "${testTagPrefix}_increment"
        )
      }
    }
  }
}

@Composable
private fun TactileCircleButton(
  icon: ImageVector,
  enabled: Boolean,
  isHighlight: Boolean = false,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.9f else 1.0f,
    animationSpec = spring(stiffness = 600f),
    label = "tactileBtnScale"
  )

  Box(
    modifier = Modifier
      .scale(scale)
      .size(34.dp)
      .clip(CircleShape)
      .background(
        when {
          !enabled -> FameGoSurface
          isHighlight -> FameGoGoldContainer
          else -> FameGoCardElevated
        }
      )
      .border(
        1.dp,
        if (isHighlight && enabled) FameGoGold.copy(alpha = 0.4f) else FameGoBorderSubtle,
        CircleShape
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = when {
        !enabled -> FameGoTextMuted.copy(alpha = 0.4f)
        isHighlight -> FameGoGold
        else -> FameGoTextPrimary
      },
      modifier = Modifier.size(16.dp)
    )
  }
}

// =============================================================================
// 7. STATUS CAPSULE (Crew Availability Control: OFF DUTY <-> READY FOR SHOOTS)
// =============================================================================

@Composable
fun StatusCapsule(
  isAvailable: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.97f else 1.0f,
    animationSpec = spring(stiffness = 500f),
    label = "capsuleScale"
  )

  Surface(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(32.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onToggle
      )
      .testTag("crew_duty_toggle"),
    shape = RoundedCornerShape(32.dp),
    color = if (isAvailable) Color(0xFF0F2618) else FameGoSurface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isAvailable) FameGoSuccessGreen.copy(alpha = 0.4f) else FameGoBorderSubtle
    ),
    shadowElevation = if (isAvailable) 4.dp else 0.dp
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 18.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isAvailable) {
        LiveOrb(color = FameGoSuccessGreen, size = 7.dp)
      } else {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(FameGoTextMuted)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Text(
        text = if (isAvailable) "Ready for shoots" else "Off duty",
        color = if (isAvailable) FameGoSuccessGreen else FameGoTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp
      )
    }
  }
}

// =============================================================================
// 8. LIVE CARD (Active / Upcoming Shoot Card with Live Status Orb)
// =============================================================================

@Composable
fun LiveCard(
  booking: Booking,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  SoftCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    isElevated = true,
    onClick = onClick,
    testTag = "live_shoot_card"
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Top status row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          LiveOrb(
            color = when (booking.status) {
              BookingStatus.IN_PROGRESS -> FameGoGold
              BookingStatus.CONFIRMED -> FameGoSuccessGreen
              BookingStatus.SEARCHING_CREW -> FameGoAccentCyan
              else -> FameGoTextMuted
            },
            size = 8.dp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when (booking.status) {
              BookingStatus.IN_PROGRESS -> "Shoot in progress"
              BookingStatus.CONFIRMED -> "Confirmed"
              BookingStatus.SEARCHING_CREW -> "Finding crew"
              else -> "Completed"
            },
            color = when (booking.status) {
              BookingStatus.IN_PROGRESS -> FameGoGold
              BookingStatus.CONFIRMED -> FameGoSuccessGreen
              else -> FameGoTextSecondary
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
          )
        }

        Text(
          text = booking.date,
          color = FameGoTextMuted,
          fontSize = 12.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Title
      Text(
        text = booking.title,
        color = FameGoWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      // Time & Location
      Text(
        text = "${booking.time} • ${booking.durationHours} hours • ${booking.location.address.substringBefore(",")}",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp)
      )

      // Crew brief preview
      if (booking.assignedCrew.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          booking.assignedCrew.take(3).forEach { crew ->
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(FameGoCardElevated)
                .border(1.dp, FameGoGold.copy(alpha = 0.3f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = crew.crewName.take(1),
                color = FameGoGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Text(
            text = booking.assignedCrew.joinToString(", ") { it.crewName },
            color = FameGoTextMuted,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

// =============================================================================
// 9. GESTURE CARD (Swipe Action Priority Card with Accessible Buttons)
// =============================================================================

@Composable
fun GestureRequestCard(
  title: String,
  subtitle: String,
  timeText: String,
  requestedRole: String,
  payoutText: String,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
  modifier: Modifier = Modifier
) {
  SoftCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    isElevated = true,
    testTag = "crew_request_card"
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          LiveOrb(color = FameGoGold, size = 7.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "New shoot request",
            color = FameGoGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
          )
        }
        Text(
          text = payoutText,
          color = FameGoWhite,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        color = FameGoWhite,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "$timeText • $subtitle",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 3.dp)
      )

      Text(
        text = "Role: $requestedRole",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Clean Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Decline button
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = FameGoSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
          modifier = Modifier
            .weight(1f)
            .clickable(onClick = onDecline)
            .testTag("decline_request_button")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Decline",
              color = FameGoTextMuted,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Accept button (FameGo Gold)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = FameGoGold,
          modifier = Modifier
            .weight(1.5f)
            .clickable(onClick = onAccept)
            .testTag("accept_request_button")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Accept shoot",
              color = FameGoBg,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

// =============================================================================
// 10. REUSABLE UTILITIES & BACKWARD-COMPATIBLE WRAPPERS
// =============================================================================

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.padding(vertical = 8.dp)) {
    Text(
      text = title,
      color = FameGoWhite,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )
    if (subtitle != null) {
      Text(
        text = subtitle,
        color = FameGoTextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}

@Composable
fun FameGoButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  testTag: String = "primary_button"
) {
  FlowPill(
    state = FlowPillState.CUSTOM,
    customText = text,
    customIcon = icon,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    testTag = testTag
  )
}

@Composable
fun FameGoOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  borderColor: Color = FameGoBorderSubtle,
  textColor: Color = FameGoTextPrimary,
  testTag: String = "outlined_button"
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = FameGoSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
