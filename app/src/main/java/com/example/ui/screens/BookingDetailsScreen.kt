package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.BookingStatus
import com.example.model.ShootCategory
import com.example.ui.components.FlowPill
import com.example.ui.components.FlowPillState
import com.example.ui.components.LiveOrb
import com.example.ui.components.SoftCard
import com.example.ui.theme.fameGoRise
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

@Composable
fun BookingDetailsScreen(
  bookingId: String,
  onBack: () -> Unit,
  onOpenChat: (String) -> Unit,
  onRebook: (ShootCategory) -> Unit,
  onContactSupport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  val booking = bookings.firstOrNull { it.id == bookingId } ?: bookings.firstOrNull()

  val scrollState = rememberScrollState()

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
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = FameGoTextSecondary
          )
        }
        Text(
          text = "Shoot details",
          color = FameGoWhite,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        // Chat shortcut
        IconButton(
          onClick = { onOpenChat(booking?.id ?: "") },
          modifier = Modifier.testTag("call_sheet_chat_icon")
        ) {
          Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "Message",
            tint = FameGoGold
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      if (booking != null) {
        // Main Live Shoot Card
        SoftCard(
          shape = RoundedCornerShape(22.dp),
          isElevated = true,
          testTag = "call_sheet_main_card"
        ) {
          Column(modifier = Modifier.padding(22.dp)) {
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
                    BookingStatus.PENDING -> FameGoAccentCyan
                    else -> FameGoTextMuted
                  },
                  size = 8.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = when (booking.status) {
                    BookingStatus.IN_PROGRESS -> "Shoot in progress"
                    BookingStatus.CONFIRMED -> "Crew confirmed"
                    BookingStatus.COMPLETED -> "Wrap completed"
                    else -> "Finding crew"
                  },
                  color = when (booking.status) {
                    BookingStatus.IN_PROGRESS -> FameGoGold
                    BookingStatus.CONFIRMED -> FameGoSuccessGreen
                    else -> FameGoTextSecondary
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Text(
                text = "₹${booking.estimatedBudget}",
                color = FameGoWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = booking.title,
              color = FameGoWhite,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = "${booking.date} • Call time: ${booking.time} (${booking.durationHours} hours)",
              color = FameGoTextSecondary,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Venue
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = FameGoGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${booking.location.venueName}, ${booking.location.address}",
                color = FameGoTextPrimary,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Assigned Crew Roster
        if (booking.assignedCrew.isNotEmpty()) {
          Text(
            text = "Assigned crew",
            color = FameGoTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            booking.assignedCrew.forEach { crew ->
              SoftCard(
                onClick = { onOpenChat(booking.id) },
                testTag = "assigned_crew_item_${crew.crewId}"
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(FameGoCardElevated)
                      .border(1.dp, FameGoGold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = crew.crewName.take(1),
                      color = FameGoGold,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.width(14.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = crew.crewName,
                        color = FameGoWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "${crew.rating} ★",
                        color = FameGoGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                    Text(
                      text = "${crew.role.title} • Verified kit",
                      color = FameGoTextSecondary,
                      fontSize = 12.sp,
                      modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                      text = crew.gearList.joinToString(" • "),
                      color = FameGoTextMuted,
                      fontSize = 11.sp,
                      modifier = Modifier.padding(top = 2.dp)
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FameGoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
                    modifier = Modifier.clickable { onOpenChat(booking.id) }
                  ) {
                    Text(
                      text = "Message",
                      color = FameGoGold,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }

        // Brief & Scope
        Text(
          text = "Shoot brief",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SoftCard {
          Text(
            text = booking.brief.ifEmpty { "Standard production coverage as requested." },
            color = FameGoTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(110.dp))
    }

    // Fixed Bottom Action Flow Pill
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp)
    ) {
      if (booking?.status == BookingStatus.COMPLETED) {
        FlowPill(
          state = FlowPillState.CUSTOM,
          customText = "Book again",
          onClick = { onRebook(booking.category) },
          testTag = "call_sheet_rebook_pill"
        )
      } else {
        FlowPill(
          state = FlowPillState.CUSTOM,
          customText = "Message crew",
          customIcon = Icons.Default.Chat,
          onClick = { onOpenChat(booking?.id ?: "") },
          testTag = "call_sheet_chat_pill"
        )
      }
    }
  }
}
