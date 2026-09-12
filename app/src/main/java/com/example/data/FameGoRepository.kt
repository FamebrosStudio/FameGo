package com.example.data

import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.CrewProfile
import com.example.model.NotificationItem
import com.example.model.Role
import com.example.model.SavedLocation
import com.example.model.User
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory UI state boundary. Supabase can replace this without screen changes. */
object FameGoRepository {
  private val _currentUser = MutableStateFlow(User(id = "", name = "", email = "", phone = ""))
  val currentUser: StateFlow<User> = _currentUser.asStateFlow()
  private val _activeRole = MutableStateFlow(Role.CLIENT)
  val activeRole: StateFlow<Role> = _activeRole.asStateFlow()
  private val _activeSearchingBookingId = MutableStateFlow<String?>(null)
  val activeSearchingBookingId: StateFlow<String?> = _activeSearchingBookingId.asStateFlow()
  val savedLocations: List<SavedLocation> = emptyList()
  private val _crewProfiles = MutableStateFlow<List<CrewProfile>>(emptyList())
  val crewProfiles: StateFlow<List<CrewProfile>> = _crewProfiles.asStateFlow()
  private val _favoriteCrewIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteCrewIds: StateFlow<Set<String>> = _favoriteCrewIds.asStateFlow()
  private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
  private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
  private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()
  private val _isCrewAvailable = MutableStateFlow(true)
  val isCrewAvailable: StateFlow<Boolean> = _isCrewAvailable.asStateFlow()
  private val _incomingShootRequests = MutableStateFlow<List<Booking>>(emptyList())
  val incomingShootRequests: StateFlow<List<Booking>> = _incomingShootRequests.asStateFlow()

  fun setCurrentUser(user: User) { _currentUser.value = user; _activeRole.value = user.role }
  fun switchRole(role: Role) { _activeRole.value = role; _currentUser.value = _currentUser.value.copy(role = role) }
  fun setActiveSearchingBooking(bookingId: String?) { _activeSearchingBookingId.value = bookingId }

  fun toggleFavoriteCrew(crewId: String) {
    _favoriteCrewIds.value = _favoriteCrewIds.value.toMutableSet().apply { if (!add(crewId)) remove(crewId) }
  }

  fun toggleCrewAvailability(crewId: String? = null) {
    val targetId = crewId ?: _crewProfiles.value.firstOrNull { it.userId == _currentUser.value.id }?.id ?: return
    val next = !(_crewProfiles.value.firstOrNull { it.id == targetId }?.isAvailable ?: true)
    _isCrewAvailable.value = next
    _crewProfiles.value = _crewProfiles.value.map { if (it.id == targetId) it.copy(isAvailable = next) else it }
  }

  fun createBooking(newBooking: Booking): Booking {
    _bookings.value = listOf(newBooking) + _bookings.value
    _activeSearchingBookingId.value = newBooking.id
    refreshIncomingRequests()
    return newBooking
  }

  fun crewAcceptBooking(bookingId: String, crewId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    val crew = _crewProfiles.value.firstOrNull { it.id == crewId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW || booking.assignedCrew.any { it.crewId == crewId }) return
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED, assignedCrew = it.assignedCrew + crew.toAssignedMember()) else it
    }
    _crewProfiles.value = _crewProfiles.value.map { if (it.id == crewId) it.copy(isAvailable = false) else it }
    _isCrewAvailable.value = false
    refreshIncomingRequests()
  }

  fun crewDeclineBooking(bookingId: String, crewId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    addNotification(NotificationItem("", "Shoot request declined", "A crew member is unavailable for this request.", "Just now", Role.ADMIN, false, bookingId))
  }

  fun declineShootRequest(bookingId: String) = crewDeclineBooking(bookingId, "")
  fun acceptShootRequest(bookingId: String, crewMember: AssignedCrewMember) = crewAcceptBooking(bookingId, crewMember.crewId)

  fun cancelBooking(bookingId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId && it.status != BookingStatus.COMPLETED) it.copy(status = BookingStatus.CANCELLED) else it }
    if (_activeSearchingBookingId.value == bookingId) _activeSearchingBookingId.value = null
    refreshIncomingRequests()
  }

  fun adminAssignCrew(bookingId: String, crewId: String) {
    val crew = _crewProfiles.value.firstOrNull { it.id == crewId } ?: return
    assignCrewToBooking(bookingId, crew.toAssignedMember())
  }

  fun assignCrewToBooking(bookingId: String, crew: AssignedCrewMember) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED, assignedCrew = listOf(crew)) else it }
    refreshIncomingRequests()
  }

  fun adminUpdateBookingStatus(bookingId: String, newStatus: BookingStatus) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = newStatus) else it }
    refreshIncomingRequests()
  }
  fun updateBookingStatus(bookingId: String, status: BookingStatus) = adminUpdateBookingStatus(bookingId, status)
  fun adminVerifyCrew(crewId: String, status: VerificationStatus) { _crewProfiles.value = _crewProfiles.value.map { if (it.id == crewId) it.copy(verificationStatus = status) else it } }
  fun adminUpdateUserRole(newRole: Role) = switchRole(newRole)

  fun sendChatMessage(bookingId: String, text: String, senderRole: Role, senderName: String) {
    val message = text.trim()
    if (message.isEmpty() || _bookings.value.none { it.id == bookingId }) return
    val updated = (_chatMessages.value[bookingId] ?: emptyList()) + ChatMessage(bookingId = bookingId, senderName = senderName, senderRole = senderRole, message = message, timeText = "Just now", isFromMe = true)
    _chatMessages.value = _chatMessages.value + (bookingId to updated)
  }

  fun addNotification(item: NotificationItem) { _notifications.value = listOf(item) + _notifications.value }
  private fun refreshIncomingRequests() { _incomingShootRequests.value = _bookings.value.filter { it.status == BookingStatus.SEARCHING_CREW } }
  fun markAllNotificationsRead(role: Role? = null) { _notifications.value = _notifications.value.map { if (role == null || it.targetRole == role) it.copy(isRead = true) else it } }

  private fun CrewProfile.toAssignedMember() = AssignedCrewMember(id, fullName, primaryRole, phone, gearSummary, rating, verificationStatus == VerificationStatus.VERIFIED)
}
