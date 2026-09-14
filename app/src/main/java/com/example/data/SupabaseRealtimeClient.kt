package com.example.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

/**
 * Small channel adapter for booking-scoped updates. The server remains the source
 * of truth; a change only causes the repository to reload that booking's messages.
 */
object SupabaseRealtimeClient {
  private val http = OkHttpClient()
  private val sockets = mutableMapOf<String, WebSocket>()

  fun subscribeToChat(bookingId: String, onChanged: () -> Unit) {
    if (!SupabaseConfig.isConfigured || SupabaseSession.accessToken.isNullOrBlank() || bookingId.isBlank()) return
    sockets.remove(bookingId)?.close(1000, "replaced")
    val socketUrl = SupabaseConfig.baseUrl.replaceFirst("https://", "wss://") +
      "/realtime/v1/websocket?apikey=${SupabaseConfig.publishableKey}&vsn=1.0.0"
    val request = Request.Builder().url(socketUrl).build()
    sockets[bookingId] = http.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        val changes = JSONObject().apply {
          put("event", "phx_join")
          put("topic", "realtime:booking_$bookingId")
          put("ref", "1")
          put("payload", JSONObject().apply {
            put("config", JSONObject().apply {
              put("private", false)
              put("postgres_changes", org.json.JSONArray().put(JSONObject().apply {
                put("event", "*")
                put("schema", "public")
                put("table", "chat_messages")
                put("filter", "booking_id=eq.$bookingId")
              }))
            })
            put("access_token", SupabaseSession.accessToken)
          })
        }
        webSocket.send(changes.toString())
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        runCatching {
          val event = JSONObject(text).optString("event")
          if (event == "postgres_changes") onChanged()
        }
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        sockets.remove(bookingId)
      }
    })
  }

  fun unsubscribeFromChat(bookingId: String) {
    sockets.remove(bookingId)?.close(1000, "screen closed")
  }
}
