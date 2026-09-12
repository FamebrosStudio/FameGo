package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.BookingStatus
import com.example.model.Role
import com.example.model.ShootCategory
import com.example.ui.components.FameGoBottomNav
import com.example.ui.components.FameGoTopBar
import com.example.ui.components.FameGoWheelNavigation
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookAShootScreen
import com.example.ui.screens.BookShootLaunchpadScreen
import com.example.ui.screens.BookingChatScreen
import com.example.ui.screens.BookingDetailsScreen
import com.example.ui.screens.ClientBookingsScreen
import com.example.ui.screens.ClientHomeScreen
import com.example.ui.screens.ClientProfileScreen
import com.example.ui.screens.CrewHomeScreen
import com.example.ui.screens.CrewJobsScreen
import com.example.ui.screens.CrewProfileScreen
import com.example.ui.screens.CrewRequestDetailScreen
import com.example.ui.screens.CustomerSupportScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SearchingCrewScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoTheme
import com.example.ui.theme.FameGoWhite

sealed class Screen {
  object Splash : Screen()
  object Welcome : Screen()
  data class Auth(val isSignUp: Boolean = false) : Screen()
  data class Main(val tab: String = "home") : Screen()
  data class BookAShoot(val preselectedCategory: ShootCategory? = null) : Screen()
  data class SearchingCrew(val bookingId: String) : Screen()
  data class BookingDetails(val bookingId: String) : Screen()
  data class CrewRequestDetail(val bookingId: String) : Screen()
  data class BookingChat(val bookingId: String) : Screen()
  object CustomerSupport : Screen()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FameGoTheme {
        FameGoApp()
      }
    }
  }
}

@Composable
fun FameGoApp() {
  var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
  val currentUser by FameGoRepository.currentUser.collectAsState()
  val notifications by FameGoRepository.notifications.collectAsState()
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }

  // Handle Android system back button
  BackHandler(
    enabled = currentScreen !is Screen.Splash &&
      currentScreen !is Screen.Main &&
      currentScreen !is Screen.Welcome
  ) {
    when (currentScreen) {
      is Screen.Welcome -> { /* exit or stay */ }
      is Screen.Auth -> currentScreen = Screen.Welcome
      is Screen.BookAShoot -> currentScreen = Screen.Main("home")
      is Screen.SearchingCrew -> currentScreen = Screen.Main("home")
      is Screen.BookingDetails -> currentScreen = Screen.Main("bookings")
      is Screen.CrewRequestDetail -> currentScreen = Screen.Main("home")
      is Screen.BookingChat -> currentScreen = Screen.Main("home")
      is Screen.CustomerSupport -> currentScreen = Screen.Main("home")
      is Screen.Main -> { /* handled by tab */ }
      Screen.Splash -> {}
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(FameGoBg)
  ) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "screenTransition"
    ) { screen ->
      when (screen) {
        is Screen.Splash -> {
          SplashScreen(
            onFinishSplash = { currentScreen = Screen.Welcome }
          )
        }

        is Screen.Welcome -> {
          WelcomeScreen(
            onGetStarted = { currentScreen = Screen.Auth(isSignUp = true) },
            onSignIn = { currentScreen = Screen.Auth(isSignUp = false) }
          )
        }

        is Screen.Auth -> {
          AuthScreen(
            onAuthenticated = { user ->
              FameGoRepository.setCurrentUser(user)
              currentScreen = Screen.Main(tab = "home")
            },
            onBack = { currentScreen = Screen.Welcome },
            initialIsSignUp = screen.isSignUp
          )
        }

        is Screen.Main -> {
          Scaffold(
            topBar = {
              FameGoTopBar(
                currentRole = currentUser.role,
                unreadNotifications = notifications.count {
                  it.targetRole == currentUser.role && !it.isRead
                },
                onRoleClick = {},
                onNotificationsClick = { currentScreen = Screen.Main("notifications") },
                onProfileClick = { currentScreen = Screen.Main("profile") }
              )
            },
            containerColor = FameGoBg
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
            ) {
              // BackHandler for smooth wheel and router synchronization
              BackHandler(enabled = screen.tab != "home" && screen.tab != "dashboard") {
                currentScreen = Screen.Main("home")
              }

              when (currentUser.role) {
                Role.CLIENT -> {
                  val clientTabOrder = listOf("home", "bookings", "book", "notifications", "profile")

                  AnimatedContent(
                    targetState = screen.tab,
                    transitionSpec = {
                      val initialIdx = clientTabOrder.indexOf(initialState).let { if (it == -1) 0 else it }
                      val targetIdx = clientTabOrder.indexOf(targetState).let { if (it == -1) 0 else it }
                      val direction = if (targetIdx >= initialIdx) 1 else -1
                      (slideInHorizontally(
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                      ) { width -> direction * (width / 3) } + fadeIn(tween(240)))
                        .togetherWith(
                          slideOutHorizontally(
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                          ) { width -> -direction * (width / 3) } + fadeOut(tween(200))
                        )
                    },
                    label = "clientScreenTransition"
                  ) { tab ->
                    when (tab) {
                      "home", "dashboard" -> ClientHomeScreen(
                        onBookAShoot = { cat -> currentScreen = Screen.BookAShoot(cat) },
                        onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                        onOpenLiveSearch = { id -> currentScreen = Screen.SearchingCrew(id) },
                        onOpenChat = { id -> currentScreen = Screen.BookingChat(id) },
                        onViewAllBookings = { currentScreen = Screen.Main("bookings") }
                      )
                      "bookings" -> ClientBookingsScreen(
                        onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                        onBookAgain = { cat -> currentScreen = Screen.BookAShoot(cat) },
                        onNewBooking = { currentScreen = Screen.BookAShoot(null) }
                      )
                      "book" -> BookShootLaunchpadScreen(
                        onStartBooking = { cat -> currentScreen = Screen.BookAShoot(cat) }
                      )
                      "notifications", "alerts" -> NotificationsScreen(
                        onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) }
                      )
                      "profile" -> ClientProfileScreen(
                        onSwitchRole = {},
                        onOpenSupport = { currentScreen = Screen.CustomerSupport },
                        onLogout = {
                          FameGoRepository.switchRole(Role.CLIENT)
                          currentScreen = Screen.Welcome
                        }
                      )
                      else -> ClientHomeScreen(
                        onBookAShoot = { cat -> currentScreen = Screen.BookAShoot(cat) },
                        onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                        onOpenLiveSearch = { id -> currentScreen = Screen.SearchingCrew(id) },
                        onOpenChat = { id -> currentScreen = Screen.BookingChat(id) },
                        onViewAllBookings = { currentScreen = Screen.Main("bookings") }
                      )
                    }
                  }
                }

                Role.CREW -> {
                  when (screen.tab) {
                    "home" -> CrewHomeScreen(
                      onViewRequestDetail = { id -> currentScreen = Screen.CrewRequestDetail(id) },
                      onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                      onOpenChat = { id -> currentScreen = Screen.BookingChat(id) }
                    )
                    "bookings" -> CrewJobsScreen(
                      onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) }
                    )
                    "notifications" -> NotificationsScreen(
                      onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) }
                    )
                    "profile" -> CrewProfileScreen(
                      onSwitchRole = {}
                    )
                    else -> CrewHomeScreen(
                      onViewRequestDetail = { id -> currentScreen = Screen.CrewRequestDetail(id) },
                      onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                      onOpenChat = { id -> currentScreen = Screen.BookingChat(id) }
                    )
                  }
                }

                Role.ADMIN -> {
                  AdminDashboardScreen(
                    onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                    onOpenSupport = { currentScreen = Screen.CustomerSupport }
                  )
                }
              }

              // FameGo Rotating Half-Circle Navigation Wheel
              if (currentUser.role != Role.ADMIN) {
                FameGoWheelNavigation(
                  currentTab = screen.tab,
                  onNavigate = { destination ->
                    currentScreen = Screen.Main(tab = destination)
                  },
                  onOpenBookingFlow = {
                    currentScreen = Screen.BookAShoot(null)
                  },
                  modifier = Modifier.align(Alignment.BottomCenter)
                )
              }
            }
          }
        }

        is Screen.BookAShoot -> {
          BookAShootScreen(
            preselectedCategory = screen.preselectedCategory,
            onBookingSubmitted = { bookingId ->
              currentScreen = Screen.SearchingCrew(bookingId)
            },
            onCancel = { currentScreen = Screen.Main("home") }
          )
        }

        is Screen.SearchingCrew -> {
          SearchingCrewScreen(
            bookingId = screen.bookingId,
            onConfirmed = { currentScreen = Screen.BookingDetails(screen.bookingId) },
            onCancelSearch = { currentScreen = Screen.Main("home") },
            onOpenChat = { bId -> currentScreen = Screen.BookingChat(bId) },
            onOpenDetails = { bId -> currentScreen = Screen.BookingDetails(bId) }
          )
        }

        is Screen.BookingDetails -> {
          BookingDetailsScreen(
            bookingId = screen.bookingId,
            onBack = { currentScreen = Screen.Main("bookings") },
            onOpenChat = { bId -> currentScreen = Screen.BookingChat(bId) },
            onRebook = { cat -> currentScreen = Screen.BookAShoot(cat) },
            onContactSupport = { currentScreen = Screen.CustomerSupport }
          )
        }

        is Screen.CrewRequestDetail -> {
          CrewRequestDetailScreen(
            requestId = screen.bookingId,
            onAccept = {
              FameGoRepository.acceptShootRequest(
                bookingId = screen.bookingId,
                crewMember = com.example.model.AssignedCrewMember(
                  crewId = "crew_1",
                  name = "Aarav Mehta",
                  role = com.example.model.CrewRoleType.CINEMATOGRAPHER,
                  phone = "+91 98200 11223",
                  gear = "Sony FX6 Cinema Line & Rig",
                  rating = 4.95,
                  isVerified = true
                )
              )
              currentScreen = Screen.BookingDetails(screen.bookingId)
            },
            onDecline = {
              FameGoRepository.declineShootRequest(screen.bookingId)
              currentScreen = Screen.Main("home")
            },
            onBack = { currentScreen = Screen.Main("home") }
          )
        }

        is Screen.BookingChat -> {
          BookingChatScreen(
            bookingId = screen.bookingId,
            onBack = { currentScreen = Screen.Main("home") }
          )
        }

        is Screen.CustomerSupport -> {
          CustomerSupportScreen(
            onBack = { currentScreen = Screen.Main("home") }
          )
        }
      }
    }

    // Fast Persona / Role Switcher Modal
    if (showRoleSwitcherDialog) {
      AlertDialog(
        onDismissRequest = { showRoleSwitcherDialog = false },
        containerColor = FameGoCard,
        title = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Switch role",
              color = FameGoWhite,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showRoleSwitcherDialog = false }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = FameGoTextMuted)
            }
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Switch roles to try out each perspective:",
              color = FameGoTextSecondary,
              fontSize = 13.sp
            )

            // Client Option
            PersonaCard(
              title = "Client",
              subtitle = "Book shoots, track crew, and message on set",
              badgeColor = FameGoGold,
              isSelected = currentUser.role == Role.CLIENT,
              onClick = {
                FameGoRepository.switchRole(Role.CLIENT)
                showRoleSwitcherDialog = false
                currentScreen = Screen.Main("home")
              }
            )

            // Crew Option
            PersonaCard(
              title = "Crew",
              subtitle = "Accept shoot requests and manage your availability",
              badgeColor = FameGoSuccessGreen,
              isSelected = currentUser.role == Role.CREW,
              onClick = {
                FameGoRepository.switchRole(Role.CREW)
                showRoleSwitcherDialog = false
                currentScreen = Screen.Main("home")
              }
            )

            // Admin Option
            PersonaCard(
              title = "Studio admin",
              subtitle = "Oversee active shoots, dispatch crew, and manage sets",
              badgeColor = FameGoAccentCyan,
              isSelected = currentUser.role == Role.ADMIN,
              onClick = {
                FameGoRepository.switchRole(Role.ADMIN)
                showRoleSwitcherDialog = false
                currentScreen = Screen.Main("home")
              }
            )
          }
        },
        confirmButton = {}
      )
    }
  }
}

@Composable
fun PersonaCard(
  title: String,
  subtitle: String,
  badgeColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) FameGoCardElevated else FameGoSurface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isSelected) badgeColor else FameGoBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(badgeColor)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, color = FameGoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = FameGoTextSecondary, fontSize = 11.sp)
      }
    }
  }
}

// Kept for backward compatibility with test suite
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier, color = FameGoWhite)
}
