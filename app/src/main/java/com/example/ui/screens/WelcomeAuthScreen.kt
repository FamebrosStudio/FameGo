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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Role
import com.example.ui.components.FameGoButton
import com.example.ui.components.FameGoOutlinedButton
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoDarkerGold
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onFinishSplash: () -> Unit,
  modifier: Modifier = Modifier
) {
  LaunchedEffect(Unit) {
    delay(1500)
    onFinishSplash()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Cinematic Gold Emblem
      Box(
        modifier = Modifier
          .size(88.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(FameGoGold, FameGoDarkerGold, FameGoGoldContainer)
            )
          )
          .border(2.dp, FameGoGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = null,
          tint = FameGoBg,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "FameGo",
          color = FameGoWhite,
          fontSize = 36.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(FameGoGold)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "by Famebros Studio",
        color = FameGoTextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
      )
    }

    // Bottom branding
    Text(
      text = "Production crew on demand",
      color = FameGoGold.copy(alpha = 0.7f),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 0.5.sp,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp)
    )
  }
}

@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit,
  onSignIn: () -> Unit,
  modifier: Modifier = Modifier
) {
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
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Brand Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "FameGo",
            color = FameGoWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(FameGoGold)
          )
        }
        Text(
          text = "by Famebros Studio",
          color = FameGoTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      // Center Visual & Typography
      Column(
        modifier = Modifier.padding(vertical = 32.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = FameGoCardElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder),
          modifier = Modifier.padding(bottom = 28.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(FameGoGold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Verified film and photo crew on demand",
              color = FameGoGold,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Text(
          text = "Book cinema crew in minutes",
          color = FameGoWhite,
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 40.sp,
          letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Book verified cinematographers, photographers, drone operators and editors for your next shoot. Simple, fast and reliable.",
          color = FameGoTextSecondary,
          fontSize = 15.sp,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Three quick value badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ValuePill(title = "Verified crew")
          ValuePill(title = "Call sheets")
          ValuePill(title = "Quick booking")
        }
      }

      // Bottom Actions
      Column(
        modifier = Modifier.padding(bottom = 24.dp)
      ) {
        FameGoButton(
          text = "Get started",
          onClick = onGetStarted,
          icon = Icons.Default.ArrowForward,
          modifier = Modifier.fillMaxWidth(),
          testTag = "welcome_get_started"
        )

        Spacer(modifier = Modifier.height(12.dp))

        FameGoOutlinedButton(
          text = "Sign in",
          onClick = onSignIn,
          modifier = Modifier.fillMaxWidth(),
          testTag = "welcome_sign_in"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Famebros Studio • Mumbai",
          color = FameGoTextMuted,
          fontSize = 11.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun ValuePill(title: String) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = FameGoSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder)
  ) {
    Text(
      text = title,
      color = FameGoTextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
fun AuthScreen(
  onAuthenticated: (Role) -> Unit,
  onBack: () -> Unit,
  initialIsSignUp: Boolean = false,
  modifier: Modifier = Modifier
) {
  var isSignUp by remember { mutableStateOf(initialIsSignUp) }
  var selectedRole by remember { mutableStateOf(Role.CLIENT) }

  var fullName by remember { mutableStateOf("Kabir Sharma") }
  var email by remember { mutableStateOf("kabir@urbanbrew.in") }
  var phone by remember { mutableStateOf("+91 98201 54321") }
  var companyName by remember { mutableStateOf("Urban Brew Cafe") }
  var password by remember { mutableStateOf("••••••••") }

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
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.clickable { onBack() },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Back",
            color = FameGoTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "FameGo",
            color = FameGoWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(FameGoGold)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Heading
      Text(
        text = if (isSignUp) "Create an account" else "Sign in",
        color = FameGoWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = if (isSignUp) "Sign up to book crew or accept shoots." else "Welcome back. Sign in to your account.",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      // Role Selection Pills (Client / Crew)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(FameGoCard)
          .border(1.dp, FameGoBorder, RoundedCornerShape(12.dp))
          .padding(4.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedRole == Role.CLIENT) FameGoGoldContainer else Color.Transparent)
            .border(
              1.dp,
              if (selectedRole == Role.CLIENT) FameGoGold else Color.Transparent,
              RoundedCornerShape(8.dp)
            )
            .clickable { selectedRole = Role.CLIENT }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Client",
            color = if (selectedRole == Role.CLIENT) FameGoGold else FameGoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedRole == Role.CREW) FameGoGoldContainer else Color.Transparent)
            .border(
              1.dp,
              if (selectedRole == Role.CREW) FameGoGold else Color.Transparent,
              RoundedCornerShape(8.dp)
            )
            .clickable { selectedRole = Role.CREW }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Crew",
            color = if (selectedRole == Role.CREW) FameGoGold else FameGoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Input Fields
      if (isSignUp) {
        FameGoTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = "Full name",
          icon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (selectedRole == Role.CLIENT) {
          FameGoTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = "Company name",
            icon = Icons.Default.Business
          )
          Spacer(modifier = Modifier.height(12.dp))
        }

        FameGoTextField(
          value = phone,
          onValueChange = { phone = it },
          label = "Mobile number",
          icon = Icons.Default.Phone,
          keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      FameGoTextField(
        value = email,
        onValueChange = { email = it },
        label = "Email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
      )

      Spacer(modifier = Modifier.height(12.dp))

      FameGoTextField(
        value = password,
        onValueChange = { password = it },
        label = "Password",
        icon = Icons.Default.Lock,
        isPassword = true
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Primary CTA
      FameGoButton(
        text = if (isSignUp) "Create account" else "Sign in",
        onClick = { onAuthenticated(selectedRole) },
        modifier = Modifier.fillMaxWidth(),
        testTag = "auth_submit_button"
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Toggle between sign in and sign up
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isSignUp = !isSignUp }
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
          color = FameGoTextSecondary,
          fontSize = 13.sp
        )
        Text(
          text = if (isSignUp) "Sign in" else "Create account",
          color = FameGoGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Quick Demo Shortcuts
      Surface(
        color = FameGoCardElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Demo access",
            color = FameGoTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(FameGoCard)
                .border(1.dp, FameGoBorder, RoundedCornerShape(8.dp))
                .clickable { onAuthenticated(Role.CLIENT) }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Client", color = FameGoGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(FameGoCard)
                .border(1.dp, FameGoBorder, RoundedCornerShape(8.dp))
                .clickable { onAuthenticated(Role.CREW) }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Crew", color = FameGoSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(FameGoCard)
                .border(1.dp, FameGoBorder, RoundedCornerShape(8.dp))
                .clickable { onAuthenticated(Role.ADMIN) }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Admin", color = FameGoAccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun FameGoTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isPassword: Boolean = false,
  keyboardType: KeyboardType = KeyboardType.Text
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, color = FameGoTextMuted, fontSize = 13.sp) },
    leadingIcon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = FameGoTextMuted,
        modifier = Modifier.size(18.dp)
      )
    },
    visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    singleLine = true,
    shape = RoundedCornerShape(14.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedTextColor = FameGoTextPrimary,
      unfocusedTextColor = FameGoTextPrimary,
      focusedContainerColor = FameGoCard,
      unfocusedContainerColor = FameGoCard,
      focusedBorderColor = FameGoGold,
      unfocusedBorderColor = FameGoBorder,
      cursorColor = FameGoGold
    ),
    modifier = Modifier.fillMaxWidth()
  )
}
