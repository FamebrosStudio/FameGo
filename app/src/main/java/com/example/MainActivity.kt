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
import com.example.data.SupabaseSession
import com.example.model.BookingStatus
import com.example.model.Booking
import com.example.model.Role
import com.example.model.ShootCategory
import com.example.model.ShootPlan
import com.example.ui.components.FameGoBottomNav
import com.example.ui.components.FameGoTopBar
import com.example.ui.components.FameGoWheelNavigation
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookAShootScreen
import com.example.ui.screens.BookShootLaunchpadScreen
import com.example.ui.screens.ShootPlanScreen
import com.example.ui.screens.PaymentDemoScreen
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
  data class Auth(val startInSignUp: Boolean = false) : Screen()
  data class Main(val tab: String = "home") : Screen()
  data class ShootPlans(val preselectedCategory: ShootCategory? = null) : Screen()
  data class BookAShoot(val plan: ShootPlan, val preselectedCategory: ShootCategory? = null) : Screen()
  data class Payment(val booking: Booking) : Screen()
  data class SearchingCrew(val bookingId: String) : Screen()
  data class BookingDetails(val bookingId: String) : Screen()
  data class CrewRequestDetail(val bookingId: String) : Screen()
  data class BookingChat(val bookingId: String) : Screen()
  object CustomerSupport : Screen()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    SupabaseSession.initialize(applicationContext)
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

  // Handle Android system back button
  BackHandler(
    enabled = currentScreen !is Screen.Splash &&
      currentScreen !is Screen.Main &&
      currentScreen !is Screen.Welcome
  ) {
    when (val current = currentScreen) {
      is Screen.Welcome -> { /* exit or stay */ }
      is Screen.Auth -> currentScreen = Screen.Welcome
      is Screen.ShootPlans -> currentScreen = Screen.Main("home")
      is Screen.BookAShoot -> currentScreen = Screen.ShootPlans(current.preselectedCategory)
      is Screen.Payment -> currentScreen = Screen.BookAShoot(current.booking.plan, current.booking.category)
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
            onFinishSplash = { restoredUser ->
              currentScreen = if (restoredUser == null) Screen.Welcome else Screen.Main("home")
            }
          )
        }

        is Screen.Welcome -> {
          WelcomeScreen(
            onGetStarted = { currentScreen = Screen.Auth(startInSignUp = true) },
            onSignIn = { currentScreen = Screen.Auth(startInSignUp = false) }
          )
        }

        is Screen.Auth -> {
          AuthScreen(
            initialSignUp = screen.startInSignUp,
            onAuthenticated = { user ->
              FameGoRepository.setCurrentUser(user)
              currentScreen = Screen.Main(tab = "home")
            },
            onBack = { currentScreen = Screen.Welcome },
          )
        }

        is Screen.Main -> {
          Scaffold(
            topBar = {
              FameGoTopBar(
                currentRole = Role.CLIENT,
                unreadNotifications = notifications.count {
                  it.targetRole == Role.CLIENT && !it.isRead
                },
                onRoleClick = {},
                onNotificationsClick = { currentScreen = Screen.Main("notifications") },
                onProfileClick = { currentScreen = Screen.Main("profile") },
                roleSwitcherEnabled = false
              )
            },
            containerColor = FameGoBg
          ) { innerPadding ->
            Box(
                modifier = Modifier
                  .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
            ) {
              // Keep back navigation synchronized with the current app screen.
              BackHandler(enabled = screen.tab != "home" && screen.tab != "dashboard") {
                currentScreen = Screen.Main("home")
              }

              val clientTabOrder = listOf("home", "bookings", "book", "notifications", "profile")

              AnimatedContent(
                targetState = screen.tab,
                modifier = Modifier.fillMaxSize()
                  .navigationBarsPadding()
                  .padding(bottom = 142.dp),
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
                    onBookAShoot = { cat -> currentScreen = Screen.ShootPlans(cat) },
                    onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                    onOpenLiveSearch = { id -> currentScreen = Screen.SearchingCrew(id) },
                    onOpenChat = { id -> currentScreen = Screen.BookingChat(id) },
                    onViewAllBookings = { currentScreen = Screen.Main("bookings") }
                  )
                  "bookings" -> ClientBookingsScreen(
                    onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                    onBookAgain = { cat -> currentScreen = Screen.ShootPlans(cat) },
                    onNewBooking = { currentScreen = Screen.ShootPlans(null) }
                  )
                  "book" -> BookShootLaunchpadScreen(
                    onStartBooking = { cat -> currentScreen = Screen.ShootPlans(cat) }
                  )
                  "notifications", "alerts" -> NotificationsScreen(
                    onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) }
                  )
                  "profile" -> ClientProfileScreen(
                    onOpenSupport = { currentScreen = Screen.CustomerSupport },
                    onLogout = {
                      SupabaseSession.clear()
                      currentScreen = Screen.Welcome
                    }
                  )
                  else -> ClientHomeScreen(
                    onBookAShoot = { cat -> currentScreen = Screen.ShootPlans(cat) },
                    onOpenBooking = { id -> currentScreen = Screen.BookingDetails(id) },
                    onOpenLiveSearch = { id -> currentScreen = Screen.SearchingCrew(id) },
                    onOpenChat = { id -> currentScreen = Screen.BookingChat(id) },
                    onViewAllBookings = { currentScreen = Screen.Main("bookings") }
                  )
                }
              }

              // FameGo rotating wheel navigation (swipe left/right to switch tabs)
              FameGoWheelNavigation(
                currentTab = screen.tab,
                onNavigate = { destination ->
                  currentScreen = Screen.Main(tab = destination)
                },
                onOpenBookingFlow = {
                  currentScreen = Screen.ShootPlans(null)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
              )
            }
          }
        }

        is Screen.ShootPlans -> {
          ShootPlanScreen(
            preselectedCategory = screen.preselectedCategory,
            onContinue = { plan, category -> currentScreen = Screen.BookAShoot(plan, category) },
            onBack = { currentScreen = Screen.Main("home") }
          )
        }

        is Screen.BookAShoot -> {
          BookAShootScreen(
            plan = screen.plan,
            preselectedCategory = screen.preselectedCategory,
            onBookingReadyForPayment = { booking -> currentScreen = Screen.Payment(booking) },
            onCancel = { currentScreen = Screen.ShootPlans(screen.preselectedCategory) }
          )
        }

        is Screen.Payment -> {
          PaymentDemoScreen(
            booking = screen.booking,
            onPaid = { bookingId -> currentScreen = Screen.SearchingCrew(bookingId) },
            onBack = { currentScreen = Screen.BookAShoot(screen.booking.plan, screen.booking.category) }
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
            onRebook = { cat -> currentScreen = Screen.ShootPlans(cat) },
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
  }
}

// Kept for backward compatibility with test suite
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier, color = FameGoWhite)
}
