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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.model.User
import com.example.data.SupabaseAuthClient
import com.example.data.SupabaseRestClient
import com.example.ui.components.FameGoButton
import com.example.ui.components.FameGoLogo
import com.example.ui.components.FameGoWordmark
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
import kotlinx.coroutines.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.example.R
import androidx.compose.ui.res.painterResource

@Composable
fun SplashScreen(
  onFinishSplash: () -> Unit,
  modifier: Modifier = Modifier
) {
  LaunchedEffect(Unit) {
    // Keep the brand moment brief; a fixed 1.5s pause made every cold launch
    // feel slow even though there is no startup work to wait for.
    delay(600)
    onFinishSplash()
  }

  val transition = rememberInfiniteTransition(label = "splashGradient")
  val gradientShift by transition.animateFloat(
    initialValue = -1f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
    label = "splashGradientShift"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .drawBehind {
        val shift = size.width * gradientShift
        drawRect(
          Brush.linearGradient(
            colors = listOf(Color(0xFF2B0600), Color(0xFF763807), Color(0xFF511F03), Color(0xFF2B0600)),
            start = Offset(shift, 0f),
            end = Offset(size.width + shift, size.height)
          )
        )
      },
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(R.drawable.famego_logo),
      contentDescription = "FameGo",
      contentScale = ContentScale.Fit,
      modifier = Modifier.fillMaxWidth(0.78f).height(180.dp)
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
        FameGoWordmark(modifier = Modifier.width(108.dp))
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
  onAuthenticated: (User) -> Unit,
  onBack: () -> Unit,
  initialIsSignUp: Boolean = false,
  modifier: Modifier = Modifier
) {
  val isSignUp = false

  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isSubmitting by remember { mutableStateOf(false) }
  var authError by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

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

        FameGoWordmark(modifier = Modifier.width(96.dp))
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

      if (false) {
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
            .background(if (Role.CLIENT == Role.CLIENT) FameGoGoldContainer else Color.Transparent)
            .border(
              1.dp,
              if (Role.CLIENT == Role.CLIENT) FameGoGold else Color.Transparent,
              RoundedCornerShape(8.dp)
            )
            .clickable { }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Client",
            color = if (Role.CLIENT == Role.CLIENT) FameGoGold else FameGoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .border(
              1.dp,
              Color.Transparent,
              RoundedCornerShape(8.dp)
            )
            .clickable { }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Crew",
            color = FameGoTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
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

        if (Role.CLIENT == Role.CLIENT) {
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
        onClick = {
          authError = null
          isSubmitting = true
          coroutineScope.launch {
            val result = SupabaseAuthClient.authenticate(
              email = email.trim(), password = password, signUp = false,
              name = "", phone = "", role = Role.CLIENT.name,
              companyName = companyName.trim()
            )
            isSubmitting = false
            result.onSuccess { auth ->
              val profile = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull()
              val profileJson = profile?.let { runCatching { org.json.JSONArray(it).optJSONObject(0) }.getOrNull() }
              val displayName = profileJson?.optString("full_name").orEmpty().ifEmpty { email.substringBefore("@").ifEmpty { "User" } }
              val resolvedRole = runCatching { Role.valueOf(profileJson?.optString("role").orEmpty()) }.getOrDefault(Role.CLIENT)
              onAuthenticated(User(
                id = auth.id, name = displayName, email = auth.email,
                phone = profileJson?.optString("phone").orEmpty(), companyName = profileJson?.optString("company_name").orEmpty(), role = resolvedRole,
                avatarInitials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
                  .joinToString("") { it.first().uppercase() }.ifEmpty { "FG" }
              ))
            }.onFailure { authError = it.message ?: "Authentication failed" }
          }
        },
        enabled = !isSubmitting && email.trim().isNotEmpty() &&
          password.isNotEmpty() &&
          (!isSignUp || (fullName.trim().isNotEmpty() && phone.trim().isNotEmpty())),
        modifier = Modifier.fillMaxWidth(),
        testTag = "auth_submit_button"
      )

      authError?.let { message ->
        Text(message, color = Color(0xFFFF7B84), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (false) {
      // Toggle between sign in and sign up
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { }
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
      }

      Spacer(modifier = Modifier.height(20.dp))

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
