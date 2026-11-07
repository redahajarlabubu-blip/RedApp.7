package com.oudine.redapp.data

import com.oudine.redapp.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank() &&
            BuildConfig.SUPABASE_PUBLISHABLE_KEY != "YOUR_SUPABASE_PUBLISHABLE_KEY"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
