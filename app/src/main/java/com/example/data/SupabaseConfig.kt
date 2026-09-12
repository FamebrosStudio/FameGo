package com.example.data

import android.content.Context
import com.example.BuildConfig

/** Public Supabase client settings. The database password must never be shipped in the app. */
object SupabaseConfig {
  val baseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
  val publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY

  val isConfigured: Boolean
    get() = baseUrl.startsWith("https://") &&
      !baseUrl.contains("your-project") &&
      publishableKey.startsWith("sb_publishable_")
}

object SupabaseSession {
  private const val PREFS = "famego_session"
  private const val ACCESS = "access_token"
  private const val REFRESH = "refresh_token"
  private var context: Context? = null
  @Volatile var accessToken: String? = null
    private set
  @Volatile var refreshToken: String? = null
    private set

  fun initialize(appContext: Context) {
    context = appContext.applicationContext
    val prefs = context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    accessToken = prefs?.getString(ACCESS, null)
    refreshToken = prefs?.getString(REFRESH, null)
  }

  fun save(access: String, refresh: String) {
    accessToken = access
    refreshToken = refresh
    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
      ?.putString(ACCESS, access)?.putString(REFRESH, refresh)?.apply()
  }

  fun clear() {
    accessToken = null
    refreshToken = null
    context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
  }
}
