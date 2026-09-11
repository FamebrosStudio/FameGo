package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.BookingStatus
import com.example.model.ShootCategory
import com.example.ui.components.CharacterState
import com.example.ui.components.FameGoCharacterIllustration
import com.example.ui.components.FlowPill
import com.example.ui.components.FlowPillState
import com.example.ui.components.LiveOrb
import com.example.ui.components.SoftCard
import com.example.ui.theme.fameGoBreathe
import com.example.ui.theme.fameGoRise
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

@Composable
fun ClientBookingsScreen(
  onOpenBooking: (String) -> Unit,
  onBookAgain: (ShootCategory) -> Unit,
  onNewBooking: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  var selectedTab by remember { mutableStateOf("All") }

  val filteredBookings = when (selectedTab) {
    "Active" -> bookings.filter { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
    "Completed" -> bookings.filter { it.status == BookingStatus.COMPLETED }
    else -> bookings
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Your Shoots",
        color = FameGoWhite,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Minimal Tab Filter
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Active", "Completed").forEach { tab ->
          val isSelected = selectedTab == tab
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) FameGoGoldContainer else FameGoSurface,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) FameGoGold else FameGoBorderSubtle
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { selectedTab = tab }
              .testTag("bookings_tab_${tab.lowercase()}")
          ) {
            Text(
              text = tab,
              color = if (isSelected) FameGoGold else FameGoTextSecondary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Content or Smart Empty State
      if (filteredBookings.isEmpty()) {
        // 24. SMART EMPTY STATE
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FameGoCharacterIllustration(
              state = CharacterState.NOTHING_BOOKED,
              size = 100.dp,
              modifier = Modifier.fameGoBreathe()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No shoots booked yet",
              color = FameGoWhite,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Book your first crew in a few taps.",
              color = FameGoTextMuted,
              fontSize = 14.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
            FlowPill(
              state = FlowPillState.CUSTOM,
              customText = "Book a shoot",
              onClick = onNewBooking,
              testTag = "empty_bookings_flow_pill"
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredBookings, key = { it.id }) { booking ->
            SoftCard(
              onClick = { onOpenBooking(booking.id) },
              testTag = "booking_card_${booking.id}"
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
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
                      size = 7.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = when (booking.status) {
                        BookingStatus.IN_PROGRESS -> "IN PROGRESS"
                        BookingStatus.CONFIRMED -> "CONFIRMED"
                        BookingStatus.SEARCHING_CREW -> "SEARCHING"
                        else -> "COMPLETED"
                      },
                      color = when (booking.status) {
                        BookingStatus.IN_PROGRESS -> FameGoGold
                        BookingStatus.CONFIRMED -> FameGoSuccessGreen
                        else -> FameGoTextMuted
                      },
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.8.sp
                    )
                  }

                  Text(
                    text = "₹${booking.estimatedBudget}",
                    color = FameGoGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = booking.title,
                  color = FameGoWhite,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )

                Text(
                  text = "${booking.date} • ${booking.time} • ${booking.location.address.substringBefore(",")}",
                  color = FameGoTextSecondary,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 2.dp)
                )

                if (booking.status == BookingStatus.COMPLETED) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FameGoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
                    modifier = Modifier.clickable { onBookAgain(booking.category) }
                  ) {
                    Text(
                      text = "Book Again ↻",
                      color = FameGoGold,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(110.dp))
          }
        }
      }
    }
  }
}
