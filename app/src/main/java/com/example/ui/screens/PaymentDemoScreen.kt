package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.Booking
import com.example.model.PaymentStatus
import com.example.ui.components.FameGoButton
import com.example.ui.components.SoftCard
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoLiveRed
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch

@Composable
fun PaymentDemoScreen(
  booking: Booking,
  onPaid: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var submitting by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()
  Column(
    modifier = modifier.fillMaxSize().background(FameGoBg).statusBarsPadding()
      .navigationBarsPadding().padding(horizontal = 20.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, enabled = !submitting) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FameGoTextSecondary)
      }
      Text("Payment", color = FameGoWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    Text("Demo checkout", color = FameGoGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(start = 48.dp, bottom = 28.dp))
    SoftCard(modifier = Modifier.fillMaxWidth()) {
      Column(Modifier.padding(20.dp)) {
        Text(booking.plan.title, color = FameGoWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(booking.plan.durationLabel, color = FameGoTextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
          Text("Total", color = FameGoTextSecondary, fontSize = 14.sp)
          Text("₹${"%,d".format(booking.priceRupees)}", color = FameGoGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
    Text("No money is charged in this demo. Your booking is created after confirmation.",
      color = FameGoTextMuted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp))
    error?.let { Text(it, color = FameGoLiveRed, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp)) }
    Spacer(Modifier.weight(1f))
    if (submitting) {
      Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), Arrangement.Center, Alignment.CenterVertically) {
        CircularProgressIndicator(color = FameGoGold, strokeWidth = 2.dp)
        Text("Creating your booking…", color = FameGoTextSecondary, modifier = Modifier.padding(start = 12.dp))
      }
    } else {
      FameGoButton(
        text = "Pay now",
        onClick = {
          submitting = true; error = null
          val paidBooking = booking.copy(
            paymentStatus = PaymentStatus.PAID,
            paymentReference = "DEMO-${System.currentTimeMillis()}"
          )
          scope.launch {
            FameGoRepository.createPaidBooking(paidBooking)
              .onSuccess { onPaid(it.id) }
              .onFailure { error = FameGoRepository.friendlyMessage(it); submitting = false }
          }
        },
        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp), testTag = "payment_pay_now"
      )
    }
  }
}
