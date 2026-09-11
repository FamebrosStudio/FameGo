package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.CrewRequirement
import com.example.model.CrewRoleType
import com.example.model.ShootCategory
import com.example.model.ShootLocation
import com.example.ui.components.FlowPill
import com.example.ui.components.FlowPillState
import com.example.ui.components.FameGoOutlinedButton
import com.example.ui.components.RoleCounter
import com.example.ui.components.SoftCard
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
fun BookAShootScreen(
  preselectedCategory: ShootCategory? = null,
  onBookingSubmitted: (String) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Conversational 6-step flow (1 question per view)
  var currentStep by remember { mutableStateOf(1) }

  // Step 1: Category
  var category by remember { mutableStateOf(preselectedCategory ?: ShootCategory.VIDEO) }

  // Step 2: Date, Call Time & Duration
  var selectedDate by remember { mutableStateOf("FRI 12") }
  var callTime by remember { mutableStateOf("10:00 AM") }
  var durationHours by remember { mutableStateOf(4) }

  // Step 3: Location
  var venueName by remember { mutableStateOf("Veranda Rooftop Cafe") }
  var venueAddress by remember { mutableStateOf("Hill Road, Bandra West, Mumbai") }
  var locationNotes by remember { mutableStateOf("2nd floor, service lift available at rear.") }

  // Step 4: Crew
  val crewCounts = remember {
    mutableStateMapOf<CrewRoleType, Int>(
      CrewRoleType.CINEMATOGRAPHER to 1,
      CrewRoleType.VIDEOGRAPHER to 0,
      CrewRoleType.PHOTOGRAPHER to 0,
      CrewRoleType.DRONE_OPERATOR to 0,
      CrewRoleType.EDITOR to 0,
      CrewRoleType.ASSISTANT to 1
    )
  }

  // Step 5: Brief & Optional details
  var shootTitle by remember { mutableStateOf("Restaurant launch content") }
  var shootBrief by remember {
    mutableStateOf("Need 4 reels, interior drone-style pans, and founder talk shots.")
  }
  var showReference by remember { mutableStateOf(false) }
  var referenceLink by remember { mutableStateOf("") }
  var showInstagramLink by remember { mutableStateOf(false) }
  var instagramRef by remember { mutableStateOf("") }
  var showDriveLink by remember { mutableStateOf(false) }
  var driveLink by remember { mutableStateOf("") }
  var showInstructions by remember { mutableStateOf(false) }
  var specialInstructions by remember { mutableStateOf("") }

  // Cost calculation
  val totalCrewCount = crewCounts.values.sumOf { it }
  val estimatedCost = remember(crewCounts.values.toList(), durationHours) {
    val hourly = crewCounts.entries.sumOf { it.key.ratePerHour * it.value }
    (hourly * durationHours).coerceAtLeast(4000)
  }

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
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Top Navigation: Back / Close & Progress indicator (Tiny dots)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            if (currentStep > 1) currentStep-- else onCancel()
          },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (currentStep > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
            contentDescription = "Back",
            tint = FameGoTextSecondary
          )
        }

        // 6 Minimal progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          (1..6).forEach { stepIndex ->
            Box(
              modifier = Modifier
                .height(3.dp)
                .width(if (stepIndex == currentStep) 20.dp else 8.dp)
                .clip(CircleShape)
                .background(
                  if (stepIndex == currentStep) FameGoGold
                  else if (stepIndex < currentStep) FameGoTextMuted
                  else FameGoBorderSubtle
                )
            )
          }
        }

        // Step counter
        Text(
          text = "$currentStep of 6",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Conversational Content Body
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        AnimatedContent(
          targetState = currentStep,
          transitionSpec = {
            if (targetState > initialState) {
              slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            } else {
              slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            }
          },
          label = "wizardSteps"
        ) { step ->
          when (step) {
            // STEP 1: What are we shooting?
            1 -> StepWhatAreWeShooting(
              selected = category,
              onSelect = { category = it }
            )

            // STEP 2: When should the crew arrive?
            2 -> StepWhen(
              selectedDate = selectedDate,
              onDateSelect = { selectedDate = it },
              callTime = callTime,
              onCallTimeSelect = { callTime = it },
              duration = durationHours,
              onDurationSelect = { durationHours = it }
            )

            // STEP 3: Where are we shooting?
            3 -> StepWhere(
              venueName = venueName,
              onVenueNameChange = { venueName = it },
              address = venueAddress,
              onAddressChange = { venueAddress = it },
              locationNotes = locationNotes,
              onLocationNotesChange = { locationNotes = it }
            )

            // STEP 4: Who do you need?
            4 -> StepWhoDoYouNeed(
              crewCounts = crewCounts,
              onIncrement = { role -> crewCounts[role] = (crewCounts[role] ?: 0) + 1 },
              onDecrement = { role ->
                val curr = crewCounts[role] ?: 0
                if (curr > 0) crewCounts[role] = curr - 1
              }
            )

            // STEP 5: Tell us about the shoot
            5 -> StepTellUsAboutShoot(
              shootTitle = shootTitle,
              onShootTitleChange = { shootTitle = it },
              brief = shootBrief,
              onBriefChange = { shootBrief = it },
              showReference = showReference,
              onToggleReference = { showReference = !showReference },
              reference = referenceLink,
              onReferenceChange = { referenceLink = it },
              showInstagram = showInstagramLink,
              onToggleInstagram = { showInstagramLink = !showInstagramLink },
              instagram = instagramRef,
              onInstagramChange = { instagramRef = it },
              showDrive = showDriveLink,
              onToggleDrive = { showDriveLink = !showDriveLink },
              drive = driveLink,
              onDriveChange = { driveLink = it },
              showSpecial = showInstructions,
              onToggleSpecial = { showInstructions = !showInstructions },
              special = specialInstructions,
              onSpecialChange = { specialInstructions = it }
            )

            // STEP 6: Looks Good? (Summary)
            6 -> StepLooksGood(
              shootTitle = shootTitle,
              category = category,
              date = selectedDate,
              time = callTime,
              duration = durationHours,
              venue = venueName,
              address = venueAddress,
              crewCounts = crewCounts,
              estimatedCost = estimatedCost,
              onEdit = { currentStep = 1 }
            )
          }
        }
      }

      // Bottom Actions
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
      ) {
        if (currentStep < 6) {
          FlowPill(
            state = FlowPillState.CONTINUE,
            customText = if (currentStep == 4 && totalCrewCount == 0) "Add at least one crew member" else "Continue",
            enabled = currentStep != 4 || totalCrewCount > 0,
            onClick = {
              if (currentStep < 6) currentStep++
            },
            testTag = "wizard_continue_button"
          )
        } else {
          // Final Step: Find My Crew & Edit
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FameGoOutlinedButton(
              text = "Edit",
              onClick = { currentStep = 1 },
              modifier = Modifier.weight(1f),
              testTag = "wizard_edit_button"
            )
            FlowPill(
              state = FlowPillState.CUSTOM,
              customText = "Find My Crew",
              onClick = {
                val newBooking = Booking(
                  id = "booking_${System.currentTimeMillis()}",
                  bookingCode = "FG-" + (1000..9999).random(),
                  shootTitle = shootTitle.ifBlank { "${category.title} Shoot - $venueName" },
                  clientName = "Kabir Sharma",
                  clientCompany = "Urban Brew Cafe",
                  category = category,
                  dateText = "Friday, 12 Sep 2026",
                  timeText = callTime,
                  durationHours = durationHours,
                  venueName = venueName,
                  fullAddress = venueAddress,
                  locationInstructions = locationNotes,
                  crewRequirements = crewCounts.filter { it.value > 0 }.map { (role, count) ->
                    CrewRequirement(role = role, quantity = count)
                  },
                  shootDescription = shootBrief,
                  specialInstructions = specialInstructions,
                  brandName = "Urban Brew",
                  referenceLink = referenceLink.ifBlank { instagramRef.ifBlank { driveLink } },
                  status = BookingStatus.SEARCHING_CREW
                )
                FameGoRepository.createBooking(newBooking)
                onBookingSubmitted(newBooking.id)
              },
              modifier = Modifier.weight(1.6f),
              testTag = "wizard_find_crew_button"
            )
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 1: What are we shooting?
// -----------------------------------------------------------------------------
@Composable
private fun StepWhatAreWeShooting(
  selected: ShootCategory,
  onSelect: (ShootCategory) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "What are we shooting?",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Select a category for your shoot",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      ShootCategory.values().forEach { cat ->
        val isSelected = selected == cat
        SoftCard(
          isElevated = isSelected,
          onClick = { onSelect(cat) },
          testTag = "category_card_${cat.name.lowercase()}"
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = cat.title,
                color = if (isSelected) FameGoWhite else FameGoTextPrimary,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
              Text(
                text = cat.description,
                color = FameGoTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            if (isSelected) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(FameGoGold),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = FameGoBg,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 2: When should the crew arrive?
// -----------------------------------------------------------------------------
@Composable
private fun StepWhen(
  selectedDate: String,
  onDateSelect: (String) -> Unit,
  callTime: String,
  onCallTimeSelect: (String) -> Unit,
  duration: Int,
  onDurationSelect: (Int) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "When should the crew arrive?",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Choose your date, call time and duration",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
    )

    // Date
    Text(
      text = "Date",
      color = FameGoTextMuted,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val dates = listOf("Today", "Tomorrow", "Fri 12", "Sat 13", "Sun 14", "Mon 15")
      dates.forEach { date ->
        val isSelected = selectedDate.equals(date, ignoreCase = true)
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (isSelected) FameGoGoldContainer else FameGoCard,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FameGoGold else FameGoBorderSubtle
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onDateSelect(date) }
            .testTag("date_chip_$date")
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = date,
              color = if (isSelected) FameGoGold else FameGoTextSecondary,
              fontSize = 14.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Call Time
    Text(
      text = "Call Time",
      color = FameGoTextMuted,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("09:00 AM", "10:00 AM", "02:00 PM", "05:00 PM").forEach { time ->
        val isSelected = callTime == time
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isSelected) FameGoGoldContainer else FameGoCard,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FameGoGold else FameGoBorderSubtle
          ),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCallTimeSelect(time) }
            .testTag("time_chip_$time")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = time,
              color = if (isSelected) FameGoGold else FameGoTextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Duration (simple and direct)
    Text(
      text = "Duration",
      color = FameGoTextMuted,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(2 to "2 Hours", 4 to "4 Hours", 8 to "8 Hours").forEach { (hrs, label) ->
        val isSelected = duration == hrs
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isSelected) FameGoGoldContainer else FameGoCard,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FameGoGold else FameGoBorderSubtle
          ),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onDurationSelect(hrs) }
            .testTag("duration_chip_$hrs")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              color = if (isSelected) FameGoGold else FameGoTextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 3: Where are we shooting?
// -----------------------------------------------------------------------------
@Composable
private fun StepWhere(
  venueName: String,
  onVenueNameChange: (String) -> Unit,
  address: String,
  onAddressChange: (String) -> Unit,
  locationNotes: String,
  onLocationNotesChange: (String) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "Where are we shooting?",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Crew will navigate directly to this address",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
    )

    // Venue Name field
    OutlinedTextField(
      value = venueName,
      onValueChange = onVenueNameChange,
      label = { Text("Venue Name", color = FameGoTextMuted) },
      placeholder = { Text("e.g. Veranda Studio", color = FameGoTextMuted) },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FameGoWhite,
        unfocusedTextColor = FameGoWhite,
        focusedContainerColor = FameGoCard,
        unfocusedContainerColor = FameGoCard,
        focusedBorderColor = FameGoGold,
        unfocusedBorderColor = FameGoBorderSubtle
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("venue_name_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Address field
    OutlinedTextField(
      value = address,
      onValueChange = onAddressChange,
      label = { Text("Address", color = FameGoTextMuted) },
      placeholder = { Text("Street address", color = FameGoTextMuted) },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FameGoWhite,
        unfocusedTextColor = FameGoWhite,
        focusedContainerColor = FameGoCard,
        unfocusedContainerColor = FameGoCard,
        focusedBorderColor = FameGoGold,
        unfocusedBorderColor = FameGoBorderSubtle
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("address_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Location Notes field
    OutlinedTextField(
      value = locationNotes,
      onValueChange = onLocationNotesChange,
      label = { Text("Location Notes", color = FameGoTextMuted) },
      placeholder = { Text("Building, floor, landmark, parking details...", color = FameGoTextMuted) },
      minLines = 2,
      maxLines = 3,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FameGoWhite,
        unfocusedTextColor = FameGoWhite,
        focusedContainerColor = FameGoCard,
        unfocusedContainerColor = FameGoCard,
        focusedBorderColor = FameGoGold,
        unfocusedBorderColor = FameGoBorderSubtle
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("location_notes_input")
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Popular locations",
      color = FameGoTextMuted,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf(
        "Bandra West" to "Veranda Studio, Hill Road, Bandra West",
        "BKC Mumbai" to "One BKC, G Block, Bandra Kurla Complex",
        "Andheri West" to "Laxmi Industrial Estate, Andheri West",
        "Lower Parel" to "Phoenix Mills Compound, Lower Parel"
      ).forEach { (hub, fullAddr) ->
        SoftCard(
          onClick = {
            onVenueNameChange(hub)
            onAddressChange(fullAddr)
          },
          testTag = "hub_${hub.lowercase().replace(" ", "_")}"
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = FameGoGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = hub, color = FameGoWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
              Text(text = fullAddr, color = FameGoTextMuted, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 4: Who do you need?
// -----------------------------------------------------------------------------
@Composable
private fun StepWhoDoYouNeed(
  crewCounts: Map<CrewRoleType, Int>,
  onIncrement: (CrewRoleType) -> Unit,
  onDecrement: (CrewRoleType) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "Who do you need?",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Select the roles for your shoot",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      CrewRoleType.values().forEach { role ->
        val count = crewCounts[role] ?: 0
        RoleCounter(
          roleTitle = role.title,
          roleSubtitle = role.recommendedGear,
          ratePerHour = role.ratePerHour,
          count = count,
          onIncrement = { onIncrement(role) },
          onDecrement = { onDecrement(role) },
          testTagPrefix = "role_${role.name.lowercase()}"
        )
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 5: Tell us about the shoot
// -----------------------------------------------------------------------------
@Composable
private fun StepTellUsAboutShoot(
  shootTitle: String,
  onShootTitleChange: (String) -> Unit,
  brief: String,
  onBriefChange: (String) -> Unit,
  showReference: Boolean,
  onToggleReference: () -> Unit,
  reference: String,
  onReferenceChange: (String) -> Unit,
  showInstagram: Boolean,
  onToggleInstagram: () -> Unit,
  instagram: String,
  onInstagramChange: (String) -> Unit,
  showDrive: Boolean,
  onToggleDrive: () -> Unit,
  drive: String,
  onDriveChange: (String) -> Unit,
  showSpecial: Boolean,
  onToggleSpecial: () -> Unit,
  special: String,
  onSpecialChange: (String) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "Tell us about the shoot",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Give your crew the key details",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    // Shoot Title
    OutlinedTextField(
      value = shootTitle,
      onValueChange = onShootTitleChange,
      label = { Text("Shoot Title", color = FameGoTextMuted) },
      placeholder = { Text("Restaurant launch content", color = FameGoTextMuted) },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FameGoWhite,
        unfocusedTextColor = FameGoWhite,
        focusedContainerColor = FameGoCard,
        unfocusedContainerColor = FameGoCard,
        focusedBorderColor = FameGoGold,
        unfocusedBorderColor = FameGoBorderSubtle
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("shoot_title_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Brief field
    OutlinedTextField(
      value = brief,
      onValueChange = onBriefChange,
      label = { Text("Brief", color = FameGoTextMuted) },
      placeholder = {
        Text(
          "Tell the crew what you need...",
          color = FameGoTextMuted,
          fontSize = 14.sp
        )
      },
      minLines = 4,
      maxLines = 6,
      shape = RoundedCornerShape(18.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FameGoWhite,
        unfocusedTextColor = FameGoWhite,
        focusedContainerColor = FameGoCard,
        unfocusedContainerColor = FameGoCard,
        focusedBorderColor = FameGoGold,
        unfocusedBorderColor = FameGoBorderSubtle
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("shoot_brief_input")
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Optional",
      color = FameGoTextMuted,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))

    // Expandable: Add reference
    ExpandableOptionalField(
      label = "Add reference",
      isExpanded = showReference,
      onToggle = onToggleReference,
      value = reference,
      onValueChange = onReferenceChange,
      placeholder = "Paste moodboard or website link"
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Expandable: Add Instagram link
    ExpandableOptionalField(
      label = "Add Instagram link",
      isExpanded = showInstagram,
      onToggle = onToggleInstagram,
      value = instagram,
      onValueChange = onInstagramChange,
      placeholder = "@username or reel link"
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Expandable: Add Drive link
    ExpandableOptionalField(
      label = "Add Drive link",
      isExpanded = showDrive,
      onToggle = onToggleDrive,
      value = drive,
      onValueChange = onDriveChange,
      placeholder = "drive.google.com/..."
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Expandable: Add instructions
    ExpandableOptionalField(
      label = "Add instructions",
      isExpanded = showSpecial,
      onToggle = onToggleSpecial,
      value = special,
      onValueChange = onSpecialChange,
      placeholder = "Special notes for the crew on set"
    )
  }
}

@Composable
private fun ExpandableOptionalField(
  label: String,
  isExpanded: Boolean,
  onToggle: () -> Unit,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String
) {
  SoftCard {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = label,
          color = FameGoTextSecondary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
        Icon(
          imageVector = Icons.Default.ExpandMore,
          contentDescription = null,
          tint = FameGoTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      if (isExpanded) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = value,
          onValueChange = onValueChange,
          placeholder = { Text(placeholder, color = FameGoTextMuted, fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = FameGoWhite,
            unfocusedTextColor = FameGoWhite,
            focusedContainerColor = FameGoCardElevated,
            unfocusedContainerColor = FameGoCardElevated,
            focusedBorderColor = FameGoGold,
            unfocusedBorderColor = FameGoBorderSubtle
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

// -----------------------------------------------------------------------------
// STEP 6: Looks Good? (Clean Single Summary Card)
// -----------------------------------------------------------------------------
@Composable
private fun StepLooksGood(
  shootTitle: String,
  category: ShootCategory,
  date: String,
  time: String,
  duration: Int,
  venue: String,
  address: String,
  crewCounts: Map<CrewRoleType, Int>,
  estimatedCost: Int,
  onEdit: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    Text(
      text = "Looks good?",
      color = FameGoWhite,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.5).sp
    )
    Text(
      text = "Review your shoot details before sending to crew",
      color = FameGoTextMuted,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    // The Single Summary Card
    SoftCard(
      shape = RoundedCornerShape(24.dp),
      isElevated = true,
      testTag = "booking_summary_card"
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        // Shoot type
        Text(
          text = shootTitle.ifBlank { "${category.title} Shoot" },
          color = FameGoGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Schedule
        Text(
          text = "$date • $time",
          color = FameGoWhite,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$duration hours",
          color = FameGoTextSecondary,
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Venue
        Text(
          text = venue,
          color = FameGoWhite,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = address,
          color = FameGoTextMuted,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Crew breakdown
        Text(
          text = "Crew",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val activeCrew = crewCounts.filter { it.value > 0 }
        activeCrew.forEach { (role, count) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "$count × ${role.title}",
              color = FameGoTextSecondary,
              fontSize = 13.sp
            )
            Text(
              text = "₹${role.ratePerHour * count * duration}",
              color = FameGoTextMuted,
              fontSize = 13.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Estimated Cost
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Estimated Total",
            color = FameGoWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "₹$estimatedCost",
            color = FameGoGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
