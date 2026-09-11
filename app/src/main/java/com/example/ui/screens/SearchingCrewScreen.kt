package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.CrewRoleType
import com.example.ui.components.FlowPill
import com.example.ui.components.FlowPillState
import com.example.ui.components.LiveOrb
import com.example.ui.components.SoftCard
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.components.CharacterState
import com.example.ui.components.FameGoCharacterIllustration
import com.example.ui.theme.fameGoBreathe
import com.example.ui.theme.fameGoSettle
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.delay

@Composable
fun SearchingCrewScreen(
  bookingId: String,
  onConfirmed: () -> Unit,
  onCancelSearch: () -> Unit,
  onOpenChat: (String) -> Unit,
  onOpenDetails: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  val booking = bookings.firstOrNull { it.id == bookingId } ?: bookings.firstOrNull()

  // Searching status progression
  val statusStages = listOf(
    "Checking availability",
    "Contacting crew nearby",
    "Confirming gear setup",
    "Almost done"
  )
  var currentStageIndex by remember { mutableIntStateOf(0) }

  // Auto-progress stage and match crew if still searching
  LaunchedEffect(booking?.status) {
    if (booking?.status == BookingStatus.SEARCHING_CREW) {
      while (currentStageIndex < statusStages.size - 1) {
        delay(2500)
        currentStageIndex++
      }
      // Trigger simulation confirmation if not yet confirmed
      delay(1500)
      if (booking.assignedCrew.isEmpty()) {
        FameGoRepository.assignCrewToBooking(
          bookingId = booking.id,
          crew = AssignedCrewMember(
            crewId = "crew_1",
            name = "Aarav Mehta",
            role = CrewRoleType.CINEMATOGRAPHER,
            phone = "+91 98200 11223",
            gear = "Sony FX6 Cinema Line & Rig",
            rating = 4.95,
            isVerified = true
          )
        )
      }
    }
  }

  val isConfirmed = booking?.status == BookingStatus.CONFIRMED || (booking?.assignedCrew?.isNotEmpty() == true)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Top bar: Close / Cancel search
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isConfirmed) {
          IconButton(
            onClick = {
              FameGoRepository.cancelBooking(booking?.id ?: bookingId)
              onCancelSearch()
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cancel",
              tint = FameGoTextMuted
            )
          }
          Text(
            text = "Cancel",
            color = FameGoTextMuted,
            fontSize = 13.sp,
            modifier = Modifier.clickable {
              FameGoRepository.cancelBooking(booking?.id ?: bookingId)
              onCancelSearch()
            }
          )
        } else {
          Spacer(modifier = Modifier.size(36.dp))
          Text(
            text = "Confirmed",
            color = FameGoSuccessGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.weight(0.2f))

      // Content Transition: Finding Orb -> Confirmed Crew Card
      AnimatedContent(
        targetState = isConfirmed,
        transitionSpec = {
          fadeIn(animationSpec = tween(400)) + slideInVertically { it / 3 } togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "searchingTransition"
      ) { confirmed ->
        if (!confirmed) {
          // -------------------------------------------------------------
          // 12. FINDING CREW EXPERIENCE
          // -------------------------------------------------------------
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Situational Production Character & Radar
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(top = 10.dp)
            ) {
              SearchOrbView()
              FameGoCharacterIllustration(
                state = CharacterState.FINDING_CREW,
                size = 110.dp,
                modifier = Modifier.fameGoBreathe()
              )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
              text = "Finding your crew",
              color = FameGoWhite,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp
            )

            val area = booking?.location?.address?.substringBefore(",") ?: "Bandra"
            Text(
              text = "Checking who is free near $area",
              color = FameGoGold,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(top = 6.dp)
            )

            Text(
              text = "This usually takes 1 to 2 minutes",
              color = FameGoTextMuted,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Booking summary snippet
            SoftCard(
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = booking?.location?.address?.substringBefore(",") ?: "Bandra West",
                  color = FameGoWhite,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "${booking?.date ?: "Friday"} • ${booking?.time ?: "10:00 AM"} • ${booking?.durationHours ?: 4} Hours",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                  text = booking?.requiredCrew?.joinToString(", ") { "${it.count} ${it.role.title}" }
                    ?: "1 Cinematographer, 1 Assistant",
                  color = FameGoTextMuted,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fast Match Simulator Button
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = FameGoGoldContainer,
              border = androidx.compose.foundation.BorderStroke(1.dp, FameGoGold.copy(alpha = 0.5f)),
              modifier = Modifier
                .clickable {
                  FameGoRepository.assignCrewToBooking(
                    bookingId = booking?.id ?: bookingId,
                    crew = AssignedCrewMember(
                      crewId = "crew_1",
                      name = "Aarav Mehta",
                      role = CrewRoleType.CINEMATOGRAPHER,
                      phone = "+91 98200 11223",
                      gear = "Sony FX6 Cinema Line & Rig",
                      rating = 4.95,
                      isVerified = true
                    )
                  )
                }
                .testTag("fast_match_now_button")
            ) {
              Text(
                text = "Fast Match Now",
                color = FameGoGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
            }
          }
        } else {
          // -------------------------------------------------------------
          // 13. CREW CONFIRMATION EXPERIENCE
          // -------------------------------------------------------------
          val assignedCrew = booking?.assignedCrew?.firstOrNull() ?: AssignedCrewMember(
            crewId = "crew_1",
            name = "Aarav Mehta",
            role = CrewRoleType.CINEMATOGRAPHER,
            phone = "+91 98200 11223",
            gear = "Sony FX6 Cinema Line & Rig",
            rating = 4.95,
            isVerified = true
          )

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Situational Character + Success Badge
            Box(
              contentAlignment = Alignment.BottomEnd,
              modifier = Modifier.padding(top = 8.dp)
            ) {
              FameGoCharacterIllustration(
                state = CharacterState.CREW_CONFIRMED,
                size = 96.dp,
                modifier = Modifier.fameGoSettle(trigger = isConfirmed)
              )
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0F2B1B))
                  .border(1.dp, FameGoSuccessGreen, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = FameGoSuccessGreen,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
              text = "Your crew is confirmed",
              color = FameGoWhite,
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp
            )

            Text(
              text = "Call sheet confirmed for ${booking?.location?.venueName ?: "Veranda Studio"}",
              color = FameGoTextMuted,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Crew Profile Card
            SoftCard(
              shape = RoundedCornerShape(22.dp),
              isElevated = true,
              modifier = Modifier.fillMaxWidth(),
              testTag = "confirmed_crew_profile_card"
            ) {
              Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Avatar
                  Box(
                    modifier = Modifier
                      .size(52.dp)
                      .clip(CircleShape)
                      .background(FameGoCardElevated)
                      .border(1.dp, FameGoGold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = assignedCrew.crewName.take(1),
                      color = FameGoGold,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.width(14.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = assignedCrew.crewName,
                        color = FameGoWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FameGoGold,
                        modifier = Modifier.size(14.dp)
                      )
                      Text(
                        text = assignedCrew.rating.toString(),
                        color = FameGoGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 2.dp)
                      )
                    }

                    Text(
                      text = assignedCrew.role.title,
                      color = FameGoTextSecondary,
                      fontSize = 13.sp
                    )

                    Text(
                      text = "Verified by Famebros Studio",
                      color = FameGoSuccessGreen,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(top = 2.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gear line
                Text(
                  text = "GEAR DEPLOYED",
                  color = FameGoTextMuted,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
                Text(
                  text = assignedCrew.gearList.joinToString(" • "),
                  color = FameGoTextSecondary,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 3.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons: Message & Details
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FameGoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { onOpenChat(booking?.id ?: "") }
                      .testTag("confirmed_message_crew_button")
                  ) {
                    Row(
                      modifier = Modifier.padding(vertical = 11.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = FameGoTextSecondary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Message",
                        color = FameGoTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FameGoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { onOpenDetails(booking?.id ?: "") }
                      .testTag("confirmed_view_details_button")
                  ) {
                    Box(
                      modifier = Modifier.padding(vertical = 11.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "Shoot Details",
                        color = FameGoTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(0.8f))

      // Bottom Action
      if (isConfirmed) {
        FlowPill(
          state = FlowPillState.CREW_CONFIRMED,
          customText = "Done ✓",
          onClick = onConfirmed,
          testTag = "confirmed_done_button"
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

// -----------------------------------------------------------------------------
// FAMEGO SEARCH ORB (Gentle pulsing central orb with restrained ambient rings)
// -----------------------------------------------------------------------------
@Composable
private fun SearchOrbView() {
  val infiniteTransition = rememberInfiniteTransition(label = "searchOrbTransition")

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  val ring1Scale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 2.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring1Scale"
  )
  val ring1Alpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring1Alpha"
  )

  Box(
    modifier = Modifier.size(160.dp),
    contentAlignment = Alignment.Center
  ) {
    // Outer expanding ring
    Box(
      modifier = Modifier
        .size(90.dp)
        .scale(ring1Scale)
        .clip(CircleShape)
        .border(1.dp, FameGoGold.copy(alpha = ring1Alpha), CircleShape)
    )

    // Soft Ambient Glow
    Box(
      modifier = Modifier
        .size(100.dp)
        .scale(pulseScale)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(FameGoGold.copy(alpha = 0.25f), Color.Transparent)
          )
        )
    )

    // Core Orb
    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(CircleShape)
        .background(FameGoCardElevated)
        .border(1.5.dp, FameGoGold, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(FameGoGold)
      )
    }
  }
}
