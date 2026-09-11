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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.AssignedCrewMember
import com.example.model.BookingStatus
import com.example.model.CrewRoleType
import com.example.ui.components.LiveOrb
import com.example.ui.components.SoftCard
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
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

@Composable
fun AdminDashboardScreen(
  onOpenBooking: (String) -> Unit,
  onOpenSupport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  val crewProfiles by FameGoRepository.crewProfiles.collectAsState()

  val activeSearches = bookings.filter { it.status == BookingStatus.SEARCHING_CREW }
  val activeShoots = bookings.filter { it.status == BookingStatus.IN_PROGRESS || it.status == BookingStatus.CONFIRMED }
  val availableCrewCount = crewProfiles.count { it.isAvailable }

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Live shoots",
        color = FameGoWhite,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      )
      Text(
        text = "Overview",
        color = FameGoTextMuted,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
      )

      // 2 High-Signal Status Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Shoots Today
        SoftCard(
          modifier = Modifier.weight(1f),
          isElevated = activeShoots.isNotEmpty(),
          shape = RoundedCornerShape(18.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Shoots today",
              color = FameGoTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${activeShoots.size + activeSearches.size}",
              color = FameGoWhite,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = if (activeSearches.isNotEmpty()) "${activeSearches.size} finding crew" else "All crew assigned",
              color = FameGoTextSecondary,
              fontSize = 11.sp
            )
          }
        }

        // Available Crew
        SoftCard(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Available crew",
              color = FameGoTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "$availableCrewCount",
              color = FameGoSuccessGreen,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Ready for shoots",
              color = FameGoTextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Section 1: Active Searches needing assignment
      if (activeSearches.isNotEmpty()) {
        Text(
          text = "Finding crew",
          color = FameGoWhite,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          activeSearches.forEach { search ->
            SoftCard(
              isElevated = true,
              onClick = { onOpenBooking(search.id) },
              testTag = "admin_search_${search.id}"
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveOrb(color = FameGoGold, size = 7.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Finding crew",
                      color = FameGoGold,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  Text(text = "₹${search.estimatedBudget}", color = FameGoWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = search.title, color = FameGoWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                  text = "${search.location.venueName} • ${search.date} • ${search.time}",
                  color = FameGoTextMuted,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Manual Assign Button
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = FameGoGold,
                  modifier = Modifier
                    .clickable {
                      FameGoRepository.assignCrewToBooking(
                        bookingId = search.id,
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
                    .testTag("admin_assign_button_${search.id}")
                ) {
                  Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Assign Aarav Mehta", color = FameGoBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Section 2: Recent bookings
      Text(
        text = "Recent bookings",
        color = FameGoWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))

      if (activeShoots.isEmpty()) {
        SoftCard {
          Text(
            text = "No shoots in progress right now.",
            color = FameGoTextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          activeShoots.forEach { shoot ->
            SoftCard(
              onClick = { onOpenBooking(shoot.id) },
              testTag = "admin_active_shoot_${shoot.id}"
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveOrb(
                      color = if (shoot.status == BookingStatus.IN_PROGRESS) FameGoGold else FameGoSuccessGreen,
                      size = 7.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = when (shoot.status) {
                        BookingStatus.IN_PROGRESS -> "Shoot in progress"
                        BookingStatus.CONFIRMED -> "Crew on the way"
                        BookingStatus.COMPLETED -> "Wrap completed"
                        else -> "Confirmed"
                      },
                      color = if (shoot.status == BookingStatus.IN_PROGRESS) FameGoGold else FameGoSuccessGreen,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }

                  Text(
                    text = shoot.date,
                    color = FameGoTextMuted,
                    fontSize = 11.sp
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = shoot.title, color = FameGoWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                  text = "${shoot.time} • ${shoot.location.venueName} • Crew: ${shoot.assignedCrew.firstOrNull()?.crewName ?: "Assigned"}",
                  color = FameGoTextMuted,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(110.dp))
    }
  }
}
