package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.ui.theme.fameGoRise
import com.example.ui.components.FameGoButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.VengeanceFaqAccordion
import com.example.ui.components.VengeanceFaqItem
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoLiveRed
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

@Composable
fun CustomerSupportScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var disputeReason by remember { mutableStateOf("Call Time Reschedule") }
  var disputeText by remember { mutableStateOf("") }
  var isSubmitted by remember { mutableStateOf(false) }
  var activeChannelDialog by remember { mutableStateOf<String?>(null) }

  val reasons = listOf("Call Time Reschedule", "Crew Equipment Question", "Shoot Location Change", "Billing / Tax Invoice", "Other Production Inquiry")

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FameGoTextPrimary)
        }
        Text(
          text = "Support",
          color = FameGoWhite,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(36.dp))
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "How can we help?",
        color = FameGoWhite,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Our team is here to help with your shoots",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      // Direct Contact Channels
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SupportChannelCard(
          icon = Icons.Default.Phone,
          title = "Call support",
          subtitle = "+91 98200 FAMEGO • Available 24/7",
          accentColor = FameGoGold,
          onClick = { activeChannelDialog = "phone" }
        )

        SupportChannelCard(
          icon = Icons.AutoMirrored.Filled.Chat,
          title = "Chat on WhatsApp",
          subtitle = "Fast crew coordination and shoot updates",
          accentColor = FameGoSuccessGreen,
          onClick = { activeChannelDialog = "whatsapp" }
        )

        SupportChannelCard(
          icon = Icons.Default.Headphones,
          title = "Priority concierge",
          subtitle = "Dedicated help for large shoots and gear packages",
          accentColor = FameGoAccentCyan,
          onClick = { activeChannelDialog = "concierge" }
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Report an Issue / Booking Dispute Card
      SectionHeader(
        title = "Report an issue",
        subtitle = "Send a note to our operations team"
      )

      Surface(
        color = FameGoCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          if (isSubmitted) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D2818)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = FameGoSuccessGreen)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Message received",
                  color = FameGoWhite,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Our team will reach out within 5 minutes.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          } else {
            Text(
              text = "Topic",
              color = FameGoTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              reasons.forEach { reason ->
                val isSelected = disputeReason == reason
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) FameGoGoldContainer else FameGoCardElevated,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) FameGoGold else FameGoBorder
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { disputeReason = reason }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = reason,
                      color = if (isSelected) FameGoGold else FameGoTextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = FameGoGold, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Tell us what happened",
              color = FameGoTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
              value = disputeText,
              onValueChange = { disputeText = it },
              placeholder = { Text("Details about timing, location, or crew...", color = FameGoTextMuted, fontSize = 12.sp) },
              minLines = 3,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FameGoTextPrimary,
                unfocusedTextColor = FameGoTextPrimary,
                focusedContainerColor = FameGoCardElevated,
                unfocusedContainerColor = FameGoCardElevated,
                focusedBorderColor = FameGoGold,
                unfocusedBorderColor = FameGoBorder
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FameGoButton(
              text = "Send message",
              onClick = {
                if (disputeText.isNotBlank()) {
                  val ok = com.example.data.FameGoRepository.submitSupportMessage("[$disputeReason] ${disputeText.trim()}")
                  if (ok) isSubmitted = true
                }
              },
              icon = Icons.AutoMirrored.Filled.Send,
              modifier = Modifier.fillMaxWidth(),
              testTag = "submit_support_ticket"
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Production FAQ
      SectionHeader(
        title = "Frequently asked questions",
        subtitle = "Policies and setup on set"
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VengeanceFaqAccordion(
          title = null,
          items = listOf(
            VengeanceFaqItem(
              question = "What happens if a call time is delayed by weather?",
              answer = "FameGo allows client-initiated standby hold up to 2 hours without additional charges. Simply notify via chat."
            ),
            VengeanceFaqItem(
              question = "What gear is guaranteed on verified shoots?",
              answer = "Every verified cinematographer brings a minimum 4K 10-bit cinema camera (FX3/A7SIII/R5C), prime lenses, and audio wireless transmitters."
            ),
            VengeanceFaqItem(
              question = "How are shoot cancellations processed?",
              answer = "Cancellations made 12 hours prior to call time receive 100% credit for future shoots. Emergency studio dispatch replaces crew immediately if unavailable."
            )
          )
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }

    // Active Channel Dialog
    activeChannelDialog?.let { channel ->
      AlertDialog(
        onDismissRequest = { activeChannelDialog = null },
        title = {
          Text(
            text = when (channel) {
              "phone" -> "Direct Hotline Support"
              "whatsapp" -> "WhatsApp Production Desk"
              "concierge" -> "Priority Concierge"
              else -> "Support Channel"
            },
            color = FameGoWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Text(
            text = when (channel) {
              "phone" -> "Calling FameGo Operations at +91 98200 FAMEGO (326346).\n\nDirect access to Mumbai & Delhi studio coordinators with 24/7 priority line for live on-set assistance."
              "whatsapp" -> "WhatsApp direct channel connected.\n\nSend location pins, callsheet updates, parking passes, or gear replacement tickets instantly with 30-second average response time."
              "concierge" -> "VIP Concierge service active for registered studios.\n\nAssisting with multi-camera live events, commercial rigs, RED/ARRI kit upgrades, and multi-day permits."
              else -> ""
            },
            color = FameGoTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
          )
        },
        confirmButton = {
          TextButton(onClick = { activeChannelDialog = null }) {
            Text("Got it", color = FameGoGold, fontWeight = FontWeight.Bold)
          }
        },
        containerColor = FameGoCard,
        shape = RoundedCornerShape(20.dp)
      )
    }
  }
}

@Composable
fun SupportChannelCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  accentColor: Color,
  onClick: (() -> Unit)? = null
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = FameGoCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = onClick != null) { onClick?.invoke() }
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accentColor.copy(alpha = 0.15f))
          .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(title, color = FameGoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = FameGoTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
      }

      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FameGoTextMuted, modifier = Modifier.size(20.dp))
    }
  }
}
