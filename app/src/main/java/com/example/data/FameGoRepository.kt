package com.example.data

import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.CrewProfile
import com.example.model.CrewRequirement
import com.example.model.CrewRoleType
import com.example.model.NotificationItem
import com.example.model.Role
import com.example.model.SavedLocation
import com.example.model.ShootCategory
import com.example.model.User
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object FameGoRepository {

  // Current logged in user details
  private val _currentUser = MutableStateFlow(
    User(
      id = "user_client_1",
      name = "Kabir Sharma",
      email = "kabir@urbanbrew.in",
      phone = "+91 98201 54321",
      companyName = "Urban Brew Cafe",
      role = Role.CLIENT,
      avatarInitials = "KS"
    )
  )
  val currentUser: StateFlow<User> = _currentUser.asStateFlow()

  // Active Role for testing / role switching
  private val _activeRole = MutableStateFlow(Role.CLIENT)
  val activeRole: StateFlow<Role> = _activeRole.asStateFlow()

  // Active searching booking ID for live radar
  private val _activeSearchingBookingId = MutableStateFlow<String?>("bk_search_02")
  val activeSearchingBookingId: StateFlow<String?> = _activeSearchingBookingId.asStateFlow()

  // Saved locations
  val savedLocations = listOf(
    SavedLocation("loc_1", "Main Studio", "Urban Brew Cafe Roastery", "Pali Hill, Bandra West, Mumbai 400050"),
    SavedLocation("loc_2", "Luxe Warehouse", "Luxe Studio Floor 4", "New Link Rd, Andheri West, Mumbai 400053"),
    SavedLocation("loc_3", "BKC Headquarters", "One BKC Tower B", "Bandra Kurla Complex, Mumbai 400051")
  )

  // Crew Profiles
  private val _crewProfiles = MutableStateFlow(
    listOf(
      CrewProfile(
        id = "crew_1",
        userId = "user_crew_1",
        fullName = "Aarav Mehta",
        phone = "+91 98200 11223",
        email = "aarav.dp@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.CINEMATOGRAPHER,
        secondaryRoles = listOf(CrewRoleType.VIDEOGRAPHER),
        experienceYears = 6,
        bio = "Cinematographer specializing in narrative commercials, food reels and fashion films. Sony FX6 owner.",
        gearSummary = "Sony FX6 Cinema Line, DZOFilm Vespid Primes, DJI RS3 Pro Gimbal, Aputure 300d II",
        portfolioUrl = "https://famebros.studio/crew/aarav-mehta",
        instagramHandle = "@aarav.cinematography",
        verificationStatus = VerificationStatus.VERIFIED,
        isAvailable = true,
        rating = 4.95,
        totalShootsCompleted = 42,
        avatarInitials = "AM"
      ),
      CrewProfile(
        id = "crew_2",
        userId = "user_crew_2",
        fullName = "Rohan Shah",
        phone = "+91 97110 44556",
        email = "rohan.video@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.VIDEOGRAPHER,
        secondaryRoles = listOf(CrewRoleType.EDITOR),
        experienceYears = 4,
        bio = "Run-and-gun commercial shooter with rapid social cuts and high-energy music & event filming.",
        gearSummary = "Sony A7S III + Sony 24-70mm GM II, Ronin RS3, Rode Wireless PRO Audio",
        portfolioUrl = "https://famebros.studio/crew/rohan-shah",
        instagramHandle = "@rohan.shoots",
        verificationStatus = VerificationStatus.VERIFIED,
        isAvailable = true,
        rating = 4.88,
        totalShootsCompleted = 31,
        avatarInitials = "RS"
      ),
      CrewProfile(
        id = "crew_3",
        userId = "user_crew_3",
        fullName = "Sameer Khan",
        phone = "+91 98330 99887",
        email = "sameer.photo@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.PHOTOGRAPHER,
        secondaryRoles = emptyList(),
        experienceYears = 7,
        bio = "High-fashion & luxury product photographer. Published in Vogue India and Grazia.",
        gearSummary = "Canon EOS R5, Canon RF 85mm f/1.2L, Profoto B10X Plus Studio Monolights",
        portfolioUrl = "https://famebros.studio/crew/sameer-khan",
        instagramHandle = "@sameerkhan.stills",
        verificationStatus = VerificationStatus.VERIFIED,
        isAvailable = false,
        rating = 4.98,
        totalShootsCompleted = 58,
        avatarInitials = "SK"
      ),
      CrewProfile(
        id = "crew_4",
        userId = "user_crew_4",
        fullName = "Neha Kapoor",
        phone = "+91 99201 33445",
        email = "neha.edit@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.EDITOR,
        secondaryRoles = emptyList(),
        experienceYears = 5,
        bio = "Colorist and Senior Editor. Fast turnarounds on set for DIT, rough cuts and final color grading.",
        gearSummary = "MacBook Pro M3 Max, Apple Studio Display, DaVinci Resolve Studio & Speed Editor",
        portfolioUrl = "https://famebros.studio/crew/neha-kapoor",
        instagramHandle = "@neha.colorist",
        verificationStatus = VerificationStatus.VERIFIED,
        isAvailable = true,
        rating = 4.91,
        totalShootsCompleted = 47,
        avatarInitials = "NK"
      ),
      CrewProfile(
        id = "crew_5",
        userId = "user_crew_5",
        fullName = "Priya Sharma",
        phone = "+91 98199 88776",
        email = "priya.drone@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.DRONE_OPERATOR,
        secondaryRoles = listOf(CrewRoleType.VIDEOGRAPHER),
        experienceYears = 3,
        bio = "DGCA certified drone pilot. Aerial cinematics for architecture, luxury properties and car shoots.",
        gearSummary = "DJI Mavic 3 Pro Cine with Apple ProRes, ND Filters, DJI RC Pro Controller",
        portfolioUrl = "https://famebros.studio/crew/priya-drone",
        instagramHandle = "@priya.aerials",
        verificationStatus = VerificationStatus.VERIFIED,
        isAvailable = true,
        rating = 4.89,
        totalShootsCompleted = 22,
        avatarInitials = "PS"
      ),
      CrewProfile(
        id = "crew_6",
        userId = "user_crew_6",
        fullName = "Kabir Varma",
        phone = "+91 98205 66778",
        email = "kabir.assist@famego.pro",
        city = "Mumbai, MH",
        primaryRole = CrewRoleType.ASSISTANT,
        secondaryRoles = emptyList(),
        experienceYears = 2,
        bio = "Energetic production assistant and gaffer. Experienced with grip, flag rigging and power distribution.",
        gearSummary = "Gaffer Kit, C-stands, Reflector boards, Heavy Duty Sandbags & Boom Poles",
        portfolioUrl = "https://famebros.studio/crew/kabir-varma",
        instagramHandle = "@kabir.setlife",
        verificationStatus = VerificationStatus.PENDING_VERIFICATION,
        isAvailable = true,
        rating = 4.70,
        totalShootsCompleted = 14,
        avatarInitials = "KV"
      )
    )
  )
  val crewProfiles: StateFlow<List<CrewProfile>> = _crewProfiles.asStateFlow()

  // Favorite Crew IDs
  private val _favoriteCrewIds = MutableStateFlow(setOf("crew_1", "crew_2"))
  val favoriteCrewIds: StateFlow<Set<String>> = _favoriteCrewIds.asStateFlow()

  // Bookings List
  private val _bookings = MutableStateFlow(
    listOf(
      Booking(
        id = "bk_conf_01",
        bookingCode = "FG-8921",
        shootTitle = "Restaurant Launch Reel Shoot",
        clientName = "Kabir Sharma",
        clientCompany = "Urban Brew Cafe",
        category = ShootCategory.CINEMA_VIDEO,
        dateText = "12 September 2026",
        timeText = "10:00 AM Call Time",
        durationHours = 4,
        venueName = "Urban Brew Roastery",
        fullAddress = "Plot 42, Pali Hill, Bandra West, Mumbai 400050",
        locationInstructions = "Rear service entrance via Lane 3. Valet parking arranged. Ask for Manager Vikas at gate.",
        crewRequirements = listOf(
          CrewRequirement(CrewRoleType.CINEMATOGRAPHER, 1),
          CrewRequirement(CrewRoleType.ASSISTANT, 1)
        ),
        shootDescription = "Need aesthetic food closeups, espresso pour shots, interior architecture and owner interview talking heads for Instagram reels and website hero video.",
        specialInstructions = "Please bring warm cinema lights and macro lens for coffee extraction shots. 9:16 vertical priority.",
        brandName = "Urban Brew Cafe",
        referenceLink = "https://instagram.com/urbanbrewcafe",
        status = BookingStatus.CONFIRMED,
        assignedCrew = listOf(
          AssignedCrewMember(
            crewId = "crew_1",
            name = "Aarav Mehta",
            role = CrewRoleType.CINEMATOGRAPHER,
            phone = "+91 98200 11223",
            gear = "Sony FX6 + DZO Cinema Primes",
            rating = 4.95,
            isVerified = true
          )
        )
      ),
      Booking(
        id = "bk_search_02",
        bookingCode = "FG-9042",
        shootTitle = "Autumn Editorial Lookbook Campaign",
        clientName = "Ananya Singhania",
        clientCompany = "Luxe Studio",
        category = ShootCategory.FASHION,
        dateText = "15 September 2026",
        timeText = "08:30 AM Call Time",
        durationHours = 6,
        venueName = "Luxe Studio Floor 4",
        fullAddress = "Veera Desai Industrial Estate, Andheri West, Mumbai 400053",
        locationInstructions = "Take freight elevator to 4th floor. Large cyclorama studio with hair & makeup stations.",
        crewRequirements = listOf(
          CrewRequirement(CrewRoleType.PHOTOGRAPHER, 1),
          CrewRequirement(CrewRoleType.VIDEOGRAPHER, 1)
        ),
        shootDescription = "High-fashion apparel shoot featuring 12 looks on cyclorama and rooftop golden hour segments.",
        specialInstructions = "Client requires strobe lights for studio and soft diffusers for rooftop segment.",
        brandName = "Luxe Studio Bandra",
        referenceLink = "https://drive.google.com/drive/folders/moodboard-famego",
        status = BookingStatus.SEARCHING_CREW,
        assignedCrew = emptyList()
      ),
      Booking(
        id = "bk_comp_03",
        bookingCode = "FG-7832",
        shootTitle = "Avnikk Diamond Collection Showcase",
        clientName = "Rohit Avnikk",
        clientCompany = "Avnikk Jewels",
        category = ShootCategory.PRODUCT,
        dateText = "04 September 2026",
        timeText = "11:00 AM",
        durationHours = 4,
        venueName = "Avnikk Flagship Boutique",
        fullAddress = "Maker Maxity, Bandra Kurla Complex, Mumbai 400051",
        locationInstructions = "High security clearance. Valid Govt ID mandatory at building reception.",
        crewRequirements = listOf(
          CrewRequirement(CrewRoleType.PHOTOGRAPHER, 1)
        ),
        shootDescription = "Ultra high resolution macro jewelry still photography on black velvet display.",
        specialInstructions = "Clean micro-fiber gloves will be provided by client for handling jewels.",
        brandName = "Avnikk Jewels",
        status = BookingStatus.COMPLETED,
        assignedCrew = listOf(
          AssignedCrewMember(
            crewId = "crew_3",
            name = "Sameer Khan",
            role = CrewRoleType.PHOTOGRAPHER,
            phone = "+91 98330 99887",
            gear = "Canon R5 + 100mm Macro + Profoto",
            rating = 4.98,
            isVerified = true
          )
        )
      ),
      Booking(
        id = "bk_comp_04",
        bookingCode = "FG-6512",
        shootTitle = "Fintech Summit Keynote & Stage Coverage",
        clientName = "Vikram Sengupta",
        clientCompany = "Powai Tech Media",
        category = ShootCategory.CORPORATE,
        dateText = "28 August 2026",
        timeText = "09:00 AM",
        durationHours = 8,
        venueName = "The Westin Mumbai",
        fullAddress = "International Business Park, Powai, Mumbai 400076",
        locationInstructions = "Grand Ballroom A. Technical riser reserved behind stage left.",
        crewRequirements = listOf(
          CrewRequirement(CrewRoleType.VIDEOGRAPHER, 1),
          CrewRequirement(CrewRoleType.EDITOR, 1)
        ),
        shootDescription = "Full event video capture, founder interviews and rapid on-set summary edit for same-day social release.",
        specialInstructions = "Direct XLR audio feed available from sound engineer console.",
        brandName = "Powai Tech Media",
        status = BookingStatus.COMPLETED,
        assignedCrew = listOf(
          AssignedCrewMember(
            crewId = "crew_2",
            name = "Rohan Shah",
            role = CrewRoleType.VIDEOGRAPHER,
            phone = "+91 97110 44556",
            gear = "Sony A7S III + Gimbal",
            rating = 4.88,
            isVerified = true
          )
        )
      )
    )
  )
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

  // Notifications
  private val _notifications = MutableStateFlow(
    listOf(
      NotificationItem(
        title = "Crew confirmed",
        message = "Aarav Mehta is confirmed for 'Restaurant Launch Reel Shoot' on 12 September.",
        timestampText = "10m ago",
        targetRole = Role.CLIENT,
        bookingId = "bk_conf_01"
      ),
      NotificationItem(
        title = "Finding your crew",
        message = "We are checking availability for 'Autumn Editorial Lookbook Campaign' with nearby crew.",
        timestampText = "35m ago",
        targetRole = Role.CLIENT,
        bookingId = "bk_search_02"
      ),
      NotificationItem(
        title = "New shoot request nearby",
        message = "Luxe Studio needs a videographer in Andheri West for 15 September.",
        timestampText = "20m ago",
        targetRole = Role.CREW,
        bookingId = "bk_search_02"
      ),
      NotificationItem(
        title = "New shoot submitted",
        message = "Luxe Studio booked shoot FG-9042. Ready for crew matching.",
        timestampText = "35m ago",
        targetRole = Role.ADMIN,
        bookingId = "bk_search_02"
      )
    )
  )
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  // Chat Messages
  private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(
    mapOf(
      "bk_conf_01" to listOf(
        ChatMessage(
          bookingId = "bk_conf_01",
          senderName = "FameGo Dispatch",
          senderRole = Role.ADMIN,
          message = "Shoot confirmed! Aarav Mehta has been assigned as your Cinematographer. Call time is 10:00 AM at Urban Brew Roastery.",
          timeText = "10:15 AM",
          isFromMe = false
        ),
        ChatMessage(
          bookingId = "bk_conf_01",
          senderName = "Aarav Mehta",
          senderRole = Role.CREW,
          message = "Hi Kabir! I've packed the FX6 with our cinema primes and wireless monitor. Looking forward to capturing the espresso pours!",
          timeText = "10:18 AM",
          isFromMe = false
        ),
        ChatMessage(
          bookingId = "bk_conf_01",
          senderName = "Kabir Sharma",
          senderRole = Role.CLIENT,
          message = "Awesome Aarav! Vikas from our team will have the back parking gate open for you. See you on set!",
          timeText = "10:22 AM",
          isFromMe = true
        )
      )
    )
  )
  val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

  // State manipulation methods

  fun switchRole(role: Role) {
    _activeRole.value = role
    // Keep the user exposed to the UI in sync with the role selector.  Previously
    // only activeRole changed, while every screen rendered currentUser.role.
    _currentUser.value = _currentUser.value.copy(role = role)
  }

  fun setActiveSearchingBooking(bookingId: String?) {
    _activeSearchingBookingId.value = bookingId
  }

  fun toggleFavoriteCrew(crewId: String) {
    val current = _favoriteCrewIds.value.toMutableSet()
    if (current.contains(crewId)) {
      current.remove(crewId)
    } else {
      current.add(crewId)
    }
    _favoriteCrewIds.value = current
  }

  fun toggleCrewAvailability(crewId: String? = null) {
    val targetCrewId = crewId ?: _crewProfiles.value.firstOrNull {
      it.userId == _currentUser.value.id || it.id == "crew_1"
    }?.id ?: return
    val currentAvailability = _crewProfiles.value.firstOrNull { it.id == targetCrewId }?.isAvailable
      ?: _isCrewAvailable.value
    _isCrewAvailable.value = !currentAvailability
    _crewProfiles.value = _crewProfiles.value.map { crew ->
      if (crew.id == targetCrewId) {
        crew.copy(isAvailable = _isCrewAvailable.value)
      } else {
        crew
      }
    }
  }

  fun createBooking(newBooking: Booking): Booking {
    val list = _bookings.value.toMutableList()
    list.add(0, newBooking)
    _bookings.value = list
    _activeSearchingBookingId.value = newBooking.id
    refreshIncomingRequests()

    // Add notifications
    addNotification(
      NotificationItem(
        title = "Crew Search Broadcasted",
        message = "Searching verified FameGo crew for '${newBooking.shootTitle}'.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = newBooking.id
      )
    )
    addNotification(
      NotificationItem(
        title = "New Shoot Request",
        message = "${newBooking.clientCompany} requested crew in ${newBooking.venueName}.",
        timestampText = "Just now",
        targetRole = Role.CREW,
        bookingId = newBooking.id
      )
    )
    addNotification(
      NotificationItem(
        title = "New Pipeline Request",
        message = "${newBooking.bookingCode} created by ${newBooking.clientCompany}.",
        timestampText = "Just now",
        targetRole = Role.ADMIN,
        bookingId = newBooking.id
      )
    )
    return newBooking
  }

  fun crewAcceptBooking(bookingId: String, crewId: String) {
    val crew = _crewProfiles.value.find { it.id == crewId } ?: return
    val booking = _bookings.value.find { it.id == bookingId } ?: return
    // A request can only be accepted once. This also prevents duplicate crew
    // rows when a user taps Accept repeatedly during recomposition/animation.
    if (booking.status != BookingStatus.SEARCHING_CREW || booking.assignedCrew.any { it.crewId == crewId }) return
    val assigned = AssignedCrewMember(
      crewId = crew.id,
      name = crew.fullName,
      role = crew.primaryRole,
      phone = crew.phone,
      gear = crew.gearSummary,
      rating = crew.rating,
      isVerified = crew.verificationStatus == VerificationStatus.VERIFIED
    )

    _bookings.value = _bookings.value.map { b ->
      if (b.id == bookingId) {
        b.copy(
          status = BookingStatus.CONFIRMED,
          assignedCrew = b.assignedCrew + assigned
        )
      } else b
    }
    refreshIncomingRequests()

    addNotification(
      NotificationItem(
        title = "Your Crew is Confirmed!",
        message = "${crew.fullName} (${crew.primaryRole.title}) confirmed your shoot!",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
    _crewProfiles.value = _crewProfiles.value.map {
      if (it.id == crewId) it.copy(isAvailable = false) else it
    }
    _isCrewAvailable.value = false
    addNotification(
      NotificationItem(
        title = "Shoot Accepted",
        message = "You accepted the shoot. Contact client and review call sheet.",
        timestampText = "Just now",
        targetRole = Role.CREW,
        bookingId = bookingId
      )
    )
  }

  fun crewDeclineBooking(bookingId: String, crewId: String) {
    // Notify admin
    addNotification(
      NotificationItem(
        title = "Crew Declined Request",
        message = "Crew member declined request for booking $bookingId.",
        timestampText = "Just now",
        targetRole = Role.ADMIN,
        bookingId = bookingId
      )
    )
  }

  fun cancelBooking(bookingId: String) {
    _bookings.value = _bookings.value.map { b ->
      if (b.id == bookingId) {
        b.copy(status = BookingStatus.CANCELLED)
      } else b
    }
    if (_activeSearchingBookingId.value == bookingId) {
      _activeSearchingBookingId.value = null
    }
    refreshIncomingRequests()

    addNotification(
      NotificationItem(
        title = "Booking Cancelled",
        message = "Shoot request was cancelled.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
  }

  fun adminAssignCrew(bookingId: String, crewId: String) {
    val crew = _crewProfiles.value.find { it.id == crewId } ?: return
    val booking = _bookings.value.find { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    val assigned = AssignedCrewMember(
      crewId = crew.id,
      name = crew.fullName,
      role = crew.primaryRole,
      phone = crew.phone,
      gear = crew.gearSummary,
      rating = crew.rating,
      isVerified = true
    )

    _bookings.value = _bookings.value.map { b ->
      if (b.id == bookingId) {
        b.copy(
          status = BookingStatus.CONFIRMED,
          assignedCrew = listOf(assigned)
        )
      } else b
    }
    refreshIncomingRequests()
    _crewProfiles.value = _crewProfiles.value.map {
      if (it.id == crewId) it.copy(isAvailable = false) else it
    }

    addNotification(
      NotificationItem(
        title = "Crew Dispatched by Studio",
        message = "Famebros Studio assigned ${crew.fullName} to your shoot.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
  }

  fun adminUpdateBookingStatus(bookingId: String, newStatus: BookingStatus) {
    _bookings.value = _bookings.value.map { b ->
      if (b.id == bookingId) b.copy(status = newStatus) else b
    }
    refreshIncomingRequests()
  }

  fun adminVerifyCrew(crewId: String, status: VerificationStatus) {
    _crewProfiles.value = _crewProfiles.value.map { crew ->
      if (crew.id == crewId) crew.copy(verificationStatus = status) else crew
    }
  }

  fun adminUpdateUserRole(newRole: Role) {
    _currentUser.value = _currentUser.value.copy(role = newRole)
    _activeRole.value = newRole
  }

  fun sendChatMessage(bookingId: String, text: String, senderRole: Role, senderName: String) {
    if (_bookings.value.none { it.id == bookingId } || text.isBlank()) return
    val currentMap = _chatMessages.value.toMutableMap()
    val list = (currentMap[bookingId] ?: emptyList()).toMutableList()
    val newMsg = ChatMessage(
      bookingId = bookingId,
      senderName = senderName,
      senderRole = senderRole,
      message = text,
      timeText = "Just now",
      isFromMe = true
    )
    list.add(newMsg)
    currentMap[bookingId] = list
    _chatMessages.value = currentMap
  }

  fun addNotification(item: NotificationItem) {
    val list = _notifications.value.toMutableList()
    list.add(0, item)
    _notifications.value = list
  }

  // Crew availability & incoming requests for Flow UI
  private val _isCrewAvailable = MutableStateFlow(true)
  val isCrewAvailable: StateFlow<Boolean> = _isCrewAvailable.asStateFlow()

  private val _incomingShootRequests = MutableStateFlow<List<Booking>>(
    _bookings.value.filter { it.status == BookingStatus.SEARCHING_CREW }
  )
  val incomingShootRequests: StateFlow<List<Booking>> = _incomingShootRequests.asStateFlow()

  private fun refreshIncomingRequests() {
    _incomingShootRequests.value = _bookings.value.filter { it.status == BookingStatus.SEARCHING_CREW }
  }

  fun acceptShootRequest(bookingId: String, crewMember: AssignedCrewMember) {
    crewAcceptBooking(bookingId, crewMember.crewId)
  }

  fun declineShootRequest(bookingId: String) {
    crewDeclineBooking(bookingId, "crew_1")
  }

  fun assignCrewToBooking(bookingId: String, crew: AssignedCrewMember) {
    val booking = _bookings.value.find { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    _bookings.value = _bookings.value.map { b ->
      if (b.id == bookingId) {
        b.copy(
          status = BookingStatus.CONFIRMED,
          assignedCrew = listOf(crew)
        )
      } else b
    }
    refreshIncomingRequests()
    _crewProfiles.value = _crewProfiles.value.map { profile ->
      if (profile.id == crew.crewId) profile.copy(isAvailable = false) else profile
    }
    addNotification(
      NotificationItem(
        title = "Crew confirmed",
        message = "${crew.name} was assigned to '${booking.shootTitle}'.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
  }

  fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    adminUpdateBookingStatus(bookingId, status)
  }

  fun markAllNotificationsRead(role: Role? = null) {
    _notifications.value = _notifications.value.map {
      if (role == null || it.targetRole == role) it.copy(isRead = true) else it
    }
  }
}
