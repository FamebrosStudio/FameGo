package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import com.example.model.Role
import com.example.model.User
import com.example.data.SupabaseAuthClient
import com.example.data.SupabaseRestClient
import com.example.data.FameGoRepository
import com.example.ui.components.FameGoButton
import com.example.ui.components.FameGoWordmark
import com.example.ui.components.FameGoOutlinedButton
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.animation.core.tween

@Composable
fun SplashScreen(
  onFinishSplash: (User?) -> Unit,
  modifier: Modifier = Modifier
) {
  val reveal = remember { Animatable(0f) }
  val finish by rememberUpdatedState(onFinishSplash)
  LaunchedEffect(Unit) {
    val startedAt = SystemClock.elapsedRealtime()
    launch { reveal.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    val restoredUser = withTimeoutOrNull(2200) {
      runCatching { FameGoRepository.restoreSignedInUser() }.getOrNull()
    }
    val remaining = 2600L - (SystemClock.elapsedRealtime() - startedAt)
    if (remaining > 0) delay(remaining)
    finish(restoredUser)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
      .testTag("cinematic_splash"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val progress = reveal.value
      // A quiet pool of light and four viewfinder corners settle into focus.
      drawRect(Brush.radialGradient(
        listOf(Color(0xFF252017).copy(alpha = progress * 0.55f), Color.Transparent),
        center = center, radius = size.minDimension * 0.8f
      ))
      val halfWidth = size.width * (0.37f - progress * 0.04f)
      val halfHeight = size.minDimension * (0.20f - progress * 0.03f)
      val corner = 12.dp.toPx()
      val ink = FameGoGold.copy(alpha = progress * 0.40f)
      for (x in listOf(-1f, 1f)) {
        for (y in listOf(-1f, 1f)) {
          val point = center + Offset(x * halfWidth, y * halfHeight)
          drawLine(ink, point, point - Offset(x * corner, 0f), 1.dp.toPx())
          drawLine(ink, point, point - Offset(0f, y * corner), 1.dp.toPx())
        }
      }
    }
    Column(
      modifier = Modifier.graphicsLayer {
        alpha = reveal.value
        scaleX = 1.035f - reveal.value * 0.035f
        scaleY = scaleX
      },
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("FameGo", color = FameGoWhite,
        fontFamily = FontFamily(Font(com.example.R.font.megrim_regular)),
        fontSize = 42.sp, letterSpacing = 1.sp)
      Spacer(modifier = Modifier.height(14.dp))
      Text("FAMEBROS STUDIO", color = FameGoTextSecondary,
        fontSize = 9.sp, letterSpacing = 3.sp)
    }
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
          icon = Icons.AutoMirrored.Filled.ArrowForward,
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
  initialSignUp: Boolean = false,
  onAuthenticated: (User) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUp by remember(initialSignUp) { mutableStateOf(initialSignUp) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(Role.CLIENT) }
  var isSubmitting by remember { mutableStateOf(false) }
  var authError by remember { mutableStateOf<String?>(null) }
  var authNotice by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  val scrollState = rememberScrollState()
  val emailValid = email.trim().contains("@") && email.trim().contains(".")
  val passwordValid = password.length >= 6
  val signUpValid = fullName.trim().length >= 2 && phone.filter(Char::isDigit).length >= 10 &&
    password == confirmPassword
  val canSubmit = !isSubmitting && emailValid && passwordValid && (!isSignUp || signUpValid)

  fun submit() {
    authError = null
    authNotice = null
    if (!canSubmit) {
      authError = if (!emailValid) "Enter a valid email address."
        else if (!passwordValid) "Password must be at least 6 characters."
        else if (phone.filter(Char::isDigit).length < 10) "Enter a valid phone number."
        else if (password != confirmPassword) "Passwords do not match."
        else "Please complete all required fields."
      return
    }
    if (!com.example.data.SupabaseConfig.isConfigured) {
      authError = "FameGo is temporarily unavailable. Please try again later."
      return
    }
    isSubmitting = true
    coroutineScope.launch {
      val result = SupabaseAuthClient.authenticate(
        email = email.trim(), password = password, signUp = isSignUp,
        name = fullName.trim(), phone = phone.trim(), role = selectedRole.name,
        companyName = companyName.trim()
      )
      isSubmitting = false
      result.onSuccess { auth ->
        val profile = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull()
        val profileJson = profile?.let { runCatching { org.json.JSONArray(it).optJSONObject(0) }.getOrNull() }
        val displayName = profileJson?.optString("full_name").orEmpty()
          .ifEmpty { fullName.trim() }
          .ifEmpty { email.substringBefore("@").ifEmpty { "User" } }
        val resolvedRole = runCatching { Role.valueOf(profileJson?.optString("role").orEmpty()) }
          .getOrDefault(Role.CLIENT)
        onAuthenticated(User(
          id = auth.id, name = displayName, email = auth.email,
          phone = profileJson?.optString("phone").orEmpty().ifEmpty { phone.trim() },
          companyName = profileJson?.optString("company_name").orEmpty().ifEmpty { companyName.trim() },
          role = resolvedRole,
          avatarInitials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifEmpty { "FG" }
        ))
      }.onFailure { e ->
        val msg = e.message ?: "Authentication failed"
        if (msg.contains("CHECK_EMAIL") || msg.contains("confirm your email", ignoreCase = true)) {
          authNotice = "Account created. Open your email and tap \"Yes, it's me\" — the FameGo app will confirm you automatically, then sign in."
          isSignUp = false
        } else {
          authError = SupabaseAuthClient.friendlyMessage(e, signingUp = isSignUp)
        }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding()
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
        text = if (isSignUp) "Create account" else "Sign in",
        color = FameGoWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = if (isSignUp) "Join FameGo to book verified crew in minutes."
          else "Welcome back. Sign in to your account.",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Input Fields
      if (isSignUp) {
        // Role picker: client books shoots, crew receives work.
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(Role.CLIENT to "Client", Role.CREW to "Crew").forEach { (role, label) ->
            val selected = selectedRole == role
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (selected) FameGoGold else FameGoCard,
              border = androidx.compose.foundation.BorderStroke(
                1.dp, if (selected) FameGoGold else FameGoBorder
              ),
              modifier = Modifier.weight(1f).clickable { selectedRole = role }.testTag("role_${label.lowercase()}")
            ) {
              Text(
                text = if (role == Role.CLIENT) "Book shoots" else "Crew work",
                color = if (selected) Color(0xFF1A1408) else FameGoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = "Full name",
          icon = Icons.Default.Person,
          keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = phone,
          onValueChange = { phone = it },
          label = "Phone",
          icon = Icons.Default.Phone,
          keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = companyName,
          onValueChange = { companyName = it },
          label = "Company (optional)",
          icon = Icons.Default.Business,
          keyboardType = KeyboardType.Text
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
        label = "Password (min 6 chars)",
        icon = Icons.Default.Lock,
        isPassword = true
      )

      if (isSignUp) {
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          label = "Confirm password",
          icon = Icons.Default.Lock,
          isPassword = true
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Primary CTA
      FameGoButton(
        text = if (isSignUp) "Create account" else "Sign in",
        onClick = { submit() },
        enabled = canSubmit,
        modifier = Modifier.fillMaxWidth(),
        testTag = "auth_submit_button"
      )

      authError?.let { message ->
        Text(message, color = Color(0xFFFF7B84), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
      }
      authNotice?.let { message ->
        Text(message, color = FameGoGold, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth().clickable {
          isSignUp = !isSignUp
          authError = null
          authNotice = null
        },
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isSignUp) "Have an account? Sign in" else "New here? Create account",
          color = FameGoGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(vertical = 8.dp)
        )
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
