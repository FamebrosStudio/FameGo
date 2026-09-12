package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class SupabaseAuthResult(val id: String, val email: String)

/** Small REST auth adapter kept independent from the app's UI and local state. */
object SupabaseAuthClient {
  private val http = OkHttpClient()
  private val jsonType = "application/json".toMediaType()

  suspend fun authenticate(
    email: String,
    password: String,
    signUp: Boolean,
    name: String,
    phone: String,
    role: String,
    companyName: String
  ): Result<SupabaseAuthResult> = withContext(Dispatchers.IO) {
    if (!SupabaseConfig.isConfigured) return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
    val endpoint = if (signUp) {
      "${SupabaseConfig.baseUrl}/auth/v1/signup"
    } else {
      "${SupabaseConfig.baseUrl}/auth/v1/token?grant_type=password"
    }
    val body = JSONObject().apply {
      put("email", email)
      put("password", password)
      if (signUp) {
        put("data", JSONObject().apply {
          put("full_name", name)
          put("phone", phone)
          put("role", role)
          put("company_name", companyName)
        })
      }
    }.toString().toRequestBody(jsonType)
    val request = Request.Builder()
      .url(endpoint)
      .header("apikey", SupabaseConfig.publishableKey)
      .header("Authorization", "Bearer ${SupabaseConfig.publishableKey}")
      .header("Content-Type", "application/json")
      .post(body)
      .build()
    runCatching {
      http.newCall(request).execute().use { response ->
        val raw = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          val message = runCatching { JSONObject(raw).optString("msg").ifBlank { JSONObject(raw).optString("message") } }.getOrNull()
          error(message?.ifBlank { "Authentication failed (${response.code})" } ?: "Authentication failed (${response.code})")
        }
        val root = JSONObject(raw)
        val user = root.optJSONObject("user") ?: root
        SupabaseAuthResult(
          id = user.optString("id").ifBlank { error("Supabase did not return a user id") },
          email = user.optString("email", email)
        )
      }
    }
  }
}
