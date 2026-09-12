package com.example.data

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
  @Volatile var accessToken: String? = null
  fun clear() { accessToken = null }
}
